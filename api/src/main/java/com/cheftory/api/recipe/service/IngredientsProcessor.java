package com.cheftory.api.recipe.service;

import com.cheftory.api.recipe.info.UpdateRecipeInfoService;
import com.cheftory.api.recipe.info.entity.RecipeStatus;
import com.cheftory.api.recipe.ingredients.CreateRecipeIngredientsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngredientsProcessor {
    private final UpdateRecipeInfoService updateRecipeInfoService;
    private final CreateRecipeIngredientsService recipeIngredientsService;

    @Transactional
    public UUID process(UUID recipeId, String videoId) {
        updateRecipeInfoService.updateState(recipeId, RecipeStatus.CREATING_INGREDIENTS);
        return recipeIngredientsService.create(recipeId, videoId);
    }
}
