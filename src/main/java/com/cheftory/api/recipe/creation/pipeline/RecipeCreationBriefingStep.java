package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.briefing.RecipeBriefingService;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationBriefingStep implements RecipeCreationPipelineStep {
    private final RecipeBriefingService recipeBriefingService;
    private final RecipeProgressService recipeProgressService;

    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) throws RecipeException {
        if (context.getFileUri() == null || context.getMimeType() == null) {
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
        recipeProgressService.start(context.getRecipeId(), RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);
        try {
            recipeBriefingService.create(context.getVideoId(), context.getRecipeId());
            recipeProgressService.success(
                    context.getRecipeId(), RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);
            return context;
        } catch (RecipeException ex) {
            recipeProgressService.failed(
                    context.getRecipeId(), RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);
            throw ex;
        }
    }
}
