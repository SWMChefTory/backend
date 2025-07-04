package com.cheftory.api.recipe.ingredients;

import com.cheftory.api.recipe.ingredients.dto.IngredientsFindResponse;
import com.cheftory.api.recipe.ingredients.entity.Ingredients;
import com.cheftory.api.recipe.ingredients.repository.RecipeIngredientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindRecipeIngredientsService {
    private final RecipeIngredientsRepository recipeIngredientsRepository;
    public IngredientsFindResponse findIngredients(UUID recipeInfoId) {
        Ingredients ingredients = recipeIngredientsRepository
                .findByRecipeInfoId(recipeInfoId);

        return IngredientsFindResponse.from(ingredients.getContent());
    }
}
