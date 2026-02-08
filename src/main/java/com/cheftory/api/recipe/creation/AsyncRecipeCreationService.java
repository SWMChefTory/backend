package com.cheftory.api.recipe.creation;

import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
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

    @Async("recipeCreateExecutor")
    public void create(UUID recipeId, long creditCost, String videoId, URI videoUrl) {
        try {
            recipeCreationPipeline.run(RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl));

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

    private void bannedRecipe(UUID recipeId, long creditCost) {
        recipeYoutubeMetaService.ban(recipeId);
        cleanup(recipeId, creditCost);
    }

    private void failedRecipe(UUID recipeId, long creditCost) {
        recipeYoutubeMetaService.failed(recipeId);
        cleanup(recipeId, creditCost);
    }

    private void cleanup(UUID recipeId, long creditCost) {
        recipeInfoService.failed(recipeId);
        recipeProgressService.failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
        List<RecipeBookmark> bookmarks = recipeBookmarkService.gets(recipeId);
        recipeBookmarkService.deletes(
                bookmarks.stream().map(RecipeBookmark::getId).toList());

        bookmarks.forEach(h -> creditPort.refundRecipeCreate(h.getUserId(), recipeId, creditCost));
    }
}
