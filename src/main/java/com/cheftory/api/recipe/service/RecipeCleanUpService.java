package com.cheftory.api.recipe.service;

import com.cheftory.api.recipe.caption.helper.RecipeCaptionRemover;
import com.cheftory.api.recipe.helper.RecipeRemover;
import com.cheftory.api.recipe.ingredients.helper.RecipeIngredientsRemover;
import com.cheftory.api.recipe.step.helper.RecipeStepRemover;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeCleanUpService {
    private final RecipeRemover recipeRemover;
    private final RecipeCaptionRemover recipeCaptionRemover;
    private final RecipeIngredientsRemover recipeIngredientsRemover;
    private final RecipeStepRemover recipeStepRemover;

    @Transactional
    public void cleanUp(UUID recipeId) {
        recipeStepRemover.removeAllByRecipeId(recipeId);
        recipeIngredientsRemover.removeByRecipeId(recipeId);
        recipeCaptionRemover.removeByRecipeId(recipeId);
        recipeRemover.removeById(recipeId);
    }
}
