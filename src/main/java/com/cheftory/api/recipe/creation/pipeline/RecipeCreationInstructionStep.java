package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.scene.RecipeSceneService;
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
    private final RecipeSceneService recipeSceneService;
    private final RecipeProgressService recipeProgressService;

    /**
     * 조리 순서(step) 및 scene 생성 단계.
     *
     * <p>동일 `recipeId`에 step 또는 scene 데이터가 일부만 존재해도 누락된 데이터만 생성합니다.</p>
     */
    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) throws RecipeException {
        if (context.getFileUri() == null || context.getMimeType() == null) {
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
        boolean hasSteps = recipeStepService.exists(context.getRecipeId());
        boolean hasScenes = recipeSceneService.exists(context.getRecipeId());

        if (hasSteps && hasScenes) {
            recipeProgressService.success(
                    context.getRecipeId(), RecipeProgressStep.STEP, RecipeProgressDetail.STEP, context.getJobId());
            return context;
        }

        recipeProgressService.start(
                context.getRecipeId(), RecipeProgressStep.STEP, RecipeProgressDetail.STEP, context.getJobId());
        try {
            if (!hasSteps) {
                recipeStepService.create(context.getRecipeId(), context.getFileUri(), context.getMimeType());
            }
            if (!hasScenes) {
                recipeSceneService.create(context.getRecipeId(), context.getFileUri(), context.getMimeType());
            }
            recipeProgressService.success(
                    context.getRecipeId(), RecipeProgressStep.STEP, RecipeProgressDetail.STEP, context.getJobId());
            return context;
        } catch (RecipeException ex) {
            recipeProgressService.failed(
                    context.getRecipeId(), RecipeProgressStep.STEP, RecipeProgressDetail.STEP, context.getJobId());
            throw ex;
        }
    }
}
