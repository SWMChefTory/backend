package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationInstructionStep implements RecipeCreationPipelineStep {
    private final RecipeStepService recipeStepService;
    private final RecipeProgressService recipeProgressService;

    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) {
        if (context.getFileUri() == null || context.getMimeType() == null) {
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
        recipeProgressService.start(context.getRecipeId(), RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
        try {
            recipeStepService.create(context.getRecipeId(), context.getFileUri(), context.getMimeType());
            recipeProgressService.success(context.getRecipeId(), RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
            return context;
        } catch (RecipeException ex) {
            recipeProgressService.failed(context.getRecipeId(), RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
            throw ex;
        }
    }
}
