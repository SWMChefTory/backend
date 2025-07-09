package com.cheftory.api.recipe.ingredients.helper;

import com.cheftory.api.recipe.ingredients.helper.repository.RecipeIngredientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeIngredientsRemover {
    private final RecipeIngredientsRepository recipeIngredientsRepository;

    public void removeByRecipeId(UUID recipeId) {
        recipeIngredientsRepository.deleteByRecipeId(recipeId);
    }
}
