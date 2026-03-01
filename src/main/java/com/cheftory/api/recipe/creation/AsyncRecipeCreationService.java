package com.cheftory.api.recipe.creation;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationExecutionContext;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationPipeline;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 비동기 레시피 생성 서비스.
 *
 * <p>레시피 생성 워크플로우를 비동기로 실행하는 오케스트레이터입니다.
 * 파이프라인 실행 실패 시 `recipeStatus` 전이(`FAILED`/`BANNED`)와 환불/북마크 정리를 담당합니다.</p>
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AsyncRecipeCreationService {

    private final RecipeProgressService recipeProgressService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeCreditPort creditPort;
    private final RecipeCreationPipeline recipeCreationPipeline;
    private final RecipeCreationNotificationService recipeCreationNotificationService;

    /**
     * 비동기로 레시피 생성 파이프라인을 실행합니다.
     *
     * <p>외부 API 호출을 포함하는 레시피 생성 작업을 비동기로 수행하며,
     * 실패 시 크레딧 환불 및 상태 정리를 수행합니다.</p>
     *
     * @param recipeId 레시피 ID
     * @param creditCost 소비된 크레딧 양
     * @param videoId YouTube 비디오 ID
     * @param jobId 이번 비동기 생성 실행을 식별하는 진행 상태 그룹 ID
     */
    @Async("recipeCreateExecutor")
    public void create(UUID recipeId, long creditCost, String videoId, UUID jobId) {
        try {
            recipeCreationPipeline.run(RecipeCreationExecutionContext.of(recipeId, videoId, jobId));
            recipeCreationNotificationService.notify(recipeId, title);

        } catch (RecipeException e) {
            log.error("레시피 생성 실패: recipeId={}, reason={}", recipeId, e.getError(), e);
            switch (e.getError()) {
                case RecipeVerifyErrorCode.NOT_COOK_VIDEO,
                        YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_FOUND,
                        YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_EMBEDDABLE ->
                    bannedRecipe(recipeId, creditCost, jobId);
                default -> failedRecipe(recipeId, creditCost, jobId);
            }

        } catch (Exception e) {
            log.error("레시피 생성 실패(Unexpected): recipeId={}", recipeId, e);
            failedRecipe(recipeId, creditCost, jobId);
        }
    }

    private void bannedRecipe(UUID recipeId, long creditCost, UUID jobId) {
        try {
            recipeInfoService.banned(recipeId);
            cleanup(recipeId, creditCost, jobId);
        } catch (RecipeInfoException e) {
            log.error("banned handling failed. recipeId={}", recipeId, e);
        }
    }

    private void failedRecipe(UUID recipeId, long creditCost, UUID jobId) {
        try {
            recipeInfoService.failed(recipeId);
            cleanup(recipeId, creditCost, jobId);
        } catch (RecipeInfoException e) {
            log.error("failed handling failed. recipeId={}", recipeId, e);
        }
    }

    private void cleanup(UUID recipeId, long creditCost, UUID jobId) {
        // jobId 단위로 FINISHED failed 이벤트를 남기고, 생성 요청 부수효과(북마크/과금)를 정리한다.
        recipeProgressService.failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED, jobId);
        List<RecipeBookmark> bookmarks = recipeBookmarkService.gets(recipeId);
        recipeBookmarkService.deletes(
                bookmarks.stream().map(RecipeBookmark::getId).toList());

        bookmarks.forEach(h -> {
            try {
                creditPort.refundRecipeCreate(h.getUserId(), recipeId, creditCost);
            } catch (CreditException e) {
                log.warn("refund failed. recipeId={}, cost={}", recipeId, creditCost, e);
            }
        });
    }
}
