package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.verify.RecipeVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationCleanupStep {
    private final RecipeVerifyService recipeVerifyService;

    public void cleanup(RecipeCreationExecutionContext context) {
        if (context != null && context.getFileUri() != null) {
            recipeVerifyService.cleanup(context.getFileUri());
        }
    }
}
