package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.verify.RecipeVerifyService;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationVerifyStep implements RecipeCreationPipelineStep {
    private final RecipeVerifyService recipeVerifyService;
    private final RecipeProgressService recipeProgressService;

    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) throws RecipeVerifyException {

        /* TODO
         * 하위 호환성 때문에 남겨둠
         */
        recipeProgressService.start(context.getRecipeId(), RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
        try {
            RecipeVerifyClientResponse verifyResponse = recipeVerifyService.verify(context.getVideoId());

            recipeProgressService.success(
                    context.getRecipeId(), RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);

            return RecipeCreationExecutionContext.withFileInfo(
                    context, verifyResponse.fileUri(), verifyResponse.mimeType());
        } catch (RecipeException ex) {
            recipeProgressService.failed(
                    context.getRecipeId(), RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
            throw ex;
        }
    }
}
