package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.caption.RecipeCaptionService;
import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationCaptionStep implements RecipeCreationPipelineStep {
    private final RecipeCaptionService recipeCaptionService;
    private final RecipeProgressService recipeProgressService;

    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) {
        recipeProgressService.start(context.getRecipeId(), RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
        try {
            RecipeCaption caption =
                    recipeCaptionService.get(recipeCaptionService.create(context.getVideoId(), context.getRecipeId()));
            recipeProgressService.success(
                    context.getRecipeId(), RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
            return RecipeCreationExecutionContext.from(context, caption);
        } catch (RecipeException ex) {
            recipeProgressService.failed(
                    context.getRecipeId(), RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
            throw ex;
        }
    }
}
