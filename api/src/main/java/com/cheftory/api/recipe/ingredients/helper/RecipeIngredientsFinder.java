package com.cheftory.api.recipe.ingredients.helper;

import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import com.cheftory.api.recipe.ingredients.helper.repository.RecipeIngredientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeIngredientsFinder {
    private final RecipeIngredientsRepository recipeIngredientsRepository;

    public RecipeIngredients findById(UUID recipeIngredientsId) {
        return recipeIngredientsRepository
                .findById(recipeIngredientsId)
                .orElseThrow(()->new RecipeIngredientsNotFoundException("id에 해당하는 재료가 존재하지 않습니다."));
    }

    public List<Ingredient> findIngredientsContent(UUID recipeIngredientsId) {
        return findById(recipeIngredientsId)
                .getIngredients();
    }

    public RecipeIngredients findByRecipeId(UUID recipeId){
        return recipeIngredientsRepository
                .findByRecipeId(recipeId);
    }
}
