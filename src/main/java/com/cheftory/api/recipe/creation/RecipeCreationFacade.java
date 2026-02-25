package com.cheftory.api.recipe.creation;

import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeSourceType;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.dto.RecipeCreationTarget;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 생성 퍼사드.
 *
 * <p>`recipe(source_key=videoId)`를 기준으로 중복 생성 요청을 합류(join)시키고,
 * 상태(`SUCCESS/IN_PROGRESS/FAILED/BANNED/BLOCKED`)에 따라 신규 생성/재시도/차단을 라우팅합니다.
 * 실제 레시피 생성 작업은 {@link AsyncRecipeCreationService}로 비동기 위임합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeCreationFacade {

    private final AsyncRecipeCreationService asyncRecipeCreationService;
    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeYoutubeMetaService recipeYoutubeMetaService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeCreditPort creditPort;

    /**
     * 레시피 생성 대상에 따라 북마크를 생성하거나 새 레시피를 생성합니다.
     *
     * <p>동기 경로에서는 URL에서 `videoId`를 추출하고 `recipe`를 조회/생성한 뒤,
     * 상태 기반으로 기존 작업 합류 또는 비동기 생성 시작까지만 수행합니다.</p>
     *
     * @param target 레시피 생성 대상 (사용자 또는 크롤러)
     * @return 생성되거나 조회된 레시피 ID
     * @throws RecipeException 레시피 생성 실패 시
     * @throws CreditException 크레딧 처리 실패 시
     */
    public UUID create(RecipeCreationTarget target) throws RecipeException, CreditException {
        String videoId = recipeYoutubeMetaService.getVideoId(target.uri());
        try {
            RecipeInfo recipeInfo = recipeInfoService.getBySource(videoId, RecipeSourceType.YOUTUBE);
            return routeExistingRecipe(target, videoId, recipeInfo);
        } catch (RecipeException e) {
            if (e.getError() == RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND) {
                return newCreateRecipe(target, videoId);
            }
            log.warn("create failed. reason={}", e.getError(), e);
            throw e;
        }
    }

    /**
     * 기존 레시피에 요청을 합류시킵니다.
     *
     * <p>User 요청이면 북마크/과금을 수행하고, Crawler 요청이면 레시피 ID만 반환합니다.</p>
     */
    private UUID joinRecipe(RecipeCreationTarget target, RecipeInfo recipeInfo)
            throws RecipeException, CreditException {
        createBookmark(target, recipeInfo);
        return recipeInfo.getId();
    }

    /**
     * 기존 레시피 상태를 기준으로 생성 요청을 라우팅합니다.
     *
     * <p>FAILED는 retry 경로로 보내고, BLOCKED/BANNED는 차단 예외를 반환하며,
     * IN_PROGRESS/SUCCESS는 기존 레시피에 합류합니다.</p>
     *
     * @param videoId 재시도 경로에서 async 재제출에 사용하는 YouTube 비디오 ID
     */
    private UUID routeExistingRecipe(RecipeCreationTarget target, String videoId, RecipeInfo recipeInfo)
            throws RecipeException, CreditException {
        return switch (recipeInfo.getRecipeStatus()) {
            case FAILED -> retryRecipe(target, videoId, recipeInfo);
            case BLOCKED, BANNED -> throw new RecipeException(RecipeErrorCode.RECIPE_BANNED);
            case IN_PROGRESS, SUCCESS -> joinRecipe(target, recipeInfo);
        };
    }

    /**
     * retry 이후 재조회된 레시피 상태를 최종 처리합니다.
     *
     * <p>retry 후에도 FAILED면 재시작이 확정되지 않은 상태로 보고 실패를 반환합니다.</p>
     */
    private UUID joinRetryResult(RecipeCreationTarget target, RecipeInfo recipeInfo)
            throws RecipeException, CreditException {
        return switch (recipeInfo.getRecipeStatus()) {
            case FAILED -> throw new RecipeException(RecipeErrorCode.RECIPE_FAILED);
            case BLOCKED, BANNED -> throw new RecipeException(RecipeErrorCode.RECIPE_BANNED);
            case IN_PROGRESS, SUCCESS -> joinRecipe(target, recipeInfo);
        };
    }

    /**
     * 신규 레시피 생성을 시도합니다.
     *
     * <p>source 유니크 충돌 시에는 새로 만들지 않고 기존 레시피 상태 분기로 합류합니다.
     * 북마크/과금 이후 비동기 제출이 실패하면 레시피 상태를 FAILED로 되돌리고 부수효과를 롤백합니다.
     * 비동기 작업 실행 식별자는 `RecipeInfo.currentJobId`를 사용합니다.</p>
     */
    private UUID newCreateRecipe(RecipeCreationTarget target, String videoId) throws RecipeException, CreditException {
        try {
            RecipeInfo recipeInfo = recipeInfoService.create(RecipeSourceType.YOUTUBE, videoId);
            createBookmark(target, recipeInfo);
            try {
                asyncRecipeCreationService.create(
                        recipeInfo.getId(), recipeInfo.getCreditCost(), videoId, recipeInfo.getCurrentJobId());
            } catch (RuntimeException e) {
                recipeInfoService.failed(recipeInfo.getId());
                rollbackCreate(target, recipeInfo);
                throw e;
            }
            return recipeInfo.getId();
        } catch (RecipeException e) {
            if (e.getError() == RecipeInfoErrorCode.RECIPE_DUPLICATE_SOURCE) {
                RecipeInfo existing = recipeInfoService.getBySource(videoId, RecipeSourceType.YOUTUBE);
                return routeExistingRecipe(target, videoId, existing);
            }
            throw e;
        }
    }

    /**
     * 신규 생성 경로에서 비동기 제출 실패 시 생성 부수효과(북마크/과금)를 롤백합니다.
     */
    private void rollbackCreate(RecipeCreationTarget target, RecipeInfo recipeInfo) {
        switch (target) {
            case RecipeCreationTarget.User user -> {
                UUID userId = user.userId();
                try {
                    recipeBookmarkService.delete(userId, recipeInfo.getId());
                } catch (Exception e) {
                    log.warn("bookmark rollback failed. recipeId={}, userId={}", recipeInfo.getId(), userId, e);
                }

                try {
                    creditPort.refundRecipeCreate(userId, recipeInfo.getId(), recipeInfo.getCreditCost());
                } catch (CreditException e) {
                    log.warn(
                            "credit rollback failed. recipeId={}, userId={}, cost={}",
                            recipeInfo.getId(),
                            userId,
                            recipeInfo.getCreditCost(),
                            e);
                }
            }
            case RecipeCreationTarget.Crawler crawler -> {}
        }
    }

    /**
     * 실패한 레시피를 같은 recipeId로 재시도합니다.
     *
     * <p>조건부 상태 전이(FAILED -> IN_PROGRESS)에 성공한 요청만 비동기 작업을 재제출합니다.
     * 이때 `currentJobId`가 새 값으로 갱신되며, 재조회 후 갱신된 `jobId`로 async 작업을 시작합니다.
     * 이후 재조회된 상태를 기준으로 최종 join/실패/차단을 결정합니다.</p>
     */
    private UUID retryRecipe(RecipeCreationTarget target, String videoId, RecipeInfo failedRecipe)
            throws RecipeException, CreditException {
        boolean retried = recipeInfoService.retry(failedRecipe.getId());
        if (retried) {
            RecipeInfo recipeInfo = recipeInfoService.getBySource(videoId, RecipeSourceType.YOUTUBE);
            try {
                asyncRecipeCreationService.create(
                        recipeInfo.getId(), failedRecipe.getCreditCost(), videoId, recipeInfo.getCurrentJobId());
            } catch (Exception e) {
                recipeInfoService.failed(failedRecipe.getId());
                throw e;
            }
        }
        RecipeInfo existing = recipeInfoService.getBySource(videoId, RecipeSourceType.YOUTUBE);
        return joinRetryResult(target, existing);
    }

    /**
     * 요청 주체별 북마크/과금 부수효과를 처리합니다.
     *
     * <p>User 요청은 북마크 생성 후 과금하며, 과금 실패 시 북마크를 롤백합니다.
     * Crawler 요청은 아무 작업도 하지 않습니다.</p>
     */
    private void createBookmark(RecipeCreationTarget target, RecipeInfo recipeInfo)
            throws RecipeException, CreditException {
        switch (target) {
            case RecipeCreationTarget.User user -> {
                UUID userId = user.userId();

                boolean created = recipeBookmarkService.create(userId, recipeInfo.getId());
                if (!created) return;

                try {
                    creditPort.spendRecipeCreate(userId, recipeInfo.getId(), recipeInfo.getCreditCost());
                } catch (CreditException e) {
                    recipeBookmarkService.delete(userId, recipeInfo.getId());
                    switch (e.getError()) {
                        case CreditErrorCode.CREDIT_INSUFFICIENT ->
                            throw new RecipeException(CreditErrorCode.CREDIT_INSUFFICIENT, e);
                        case CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT ->
                            throw new RecipeException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT, e);
                        default -> throw e;
                    }
                }
            }
            case RecipeCreationTarget.Crawler crawler -> {}
        }
    }
}
