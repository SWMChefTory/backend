package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationFinalizeStep implements RecipeCreationPipelineStep {
    private final RecipeInfoService recipeInfoService;
    private final RecipeProgressService recipeProgressService;

    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) {
        if (context.getCaption() == null) {
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
        recipeProgressService.start(context.getRecipeId(), RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
        try {
            recipeInfoService.success(context.getRecipeId());
            recipeProgressService.success(
                    context.getRecipeId(), RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
            return context;
        } catch (RecipeException ex) {
            recipeProgressService.failed(
                    context.getRecipeId(), RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
            throw ex;
        }
    }
}
