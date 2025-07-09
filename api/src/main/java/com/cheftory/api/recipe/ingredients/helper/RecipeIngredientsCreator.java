package com.cheftory.api.recipe.ingredients.helper;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import com.cheftory.api.recipe.ingredients.helper.repository.RecipeIngredientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeIngredientsCreator {
    private final RecipeIngredientsRepository recipeIngredientsRepository;

    public UUID create(RecipeIngredients recipeIngredients) {
        return recipeIngredientsRepository.save(recipeIngredients).getId();
    }
}
