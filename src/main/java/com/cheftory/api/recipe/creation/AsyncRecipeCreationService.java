package com.cheftory.api.recipe.creation;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationExecutionContext;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationPipeline;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 비동기 레시피 생성 서비스.
 *
 * <p>외부 API 호출을 포함하는 레시피 생성 파이프라인을 비동기로 실행합니다.</p>
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AsyncRecipeCreationService {

    private final RecipeProgressService recipeProgressService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeYoutubeMetaService recipeYoutubeMetaService;
    private final RecipeIdentifyService recipeIdentifyService;
    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeCreditPort creditPort;
    private final RecipeCreationPipeline recipeCreationPipeline;

    /**
     * 비동기로 레시피 생성 파이프라인을 실행합니다.
     *
     * <p>외부 API 호출을 포함하는 레시피 생성 작업을 비동기로 수행하며,
     * 실패 시 크레딧 환불 및 식별자 정리를 수행합니다.</p>
     *
     * @param recipeId 레시피 ID
     * @param creditCost 소비된 크레딧 양
     * @param videoId YouTube 비디오 ID
     * @param videoUrl YouTube 비디오 URL
     * @throws RecipeInfoException 레시피 정보 처리 실패 시
     * @throws YoutubeMetaException YouTube 메타데이터 처리 실패 시
     */
    @Async("recipeCreateExecutor")
    public void create(UUID recipeId, long creditCost, String videoId, URI videoUrl, String title)
            throws RecipeInfoException, YoutubeMetaException {
        try {
            recipeCreationPipeline.run(RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl, title));

        } catch (RecipeException e) {
            log.error("레시피 생성 실패: recipeId={}, reason={}", recipeId, e.getError(), e);

            if (e.getError() == RecipeVerifyErrorCode.NOT_COOK_VIDEO) {
                bannedRecipe(recipeId, creditCost);
            } else {
                failedRecipe(recipeId, creditCost);
            }

        } catch (Exception e) {
            log.error("레시피 생성 실패(Unexpected): recipeId={}", recipeId, e);
            failedRecipe(recipeId, creditCost);

        } finally {
            try {
                recipeIdentifyService.delete(videoUrl);
            } catch (Exception identifyEx) {
                log.warn("identify delete 실패: recipeId={}", recipeId, identifyEx);
            }
        }
    }

    private void bannedRecipe(UUID recipeId, long creditCost) throws YoutubeMetaException, RecipeInfoException {
        recipeYoutubeMetaService.ban(recipeId);
        cleanup(recipeId, creditCost);
    }

    private void failedRecipe(UUID recipeId, long creditCost) throws RecipeInfoException {
        recipeYoutubeMetaService.failed(recipeId);
        cleanup(recipeId, creditCost);
    }

    private void cleanup(UUID recipeId, long creditCost) throws RecipeInfoException {
        recipeInfoService.failed(recipeId);
        recipeProgressService.failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
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
