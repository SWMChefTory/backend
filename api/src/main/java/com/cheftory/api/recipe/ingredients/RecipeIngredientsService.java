package com.cheftory.api.recipe.ingredients;

import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.cheftory.api.recipe.ingredients.client.RecipeIngredientsClient;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import com.cheftory.api.recipe.helper.RecipeFinder;
import com.cheftory.api.recipe.ingredients.helper.RecipeIngredientsCreator;
import com.cheftory.api.recipe.ingredients.helper.RecipeIngredientsFinder;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeIngredientsService {
    private final RecipeFinder recipeFinder;
    private final RecipeIngredientsClient recipeIngredientsClient;
    private final RecipeIngredientsCreator recipeIngredientsCreator;
    private final RecipeIngredientsFinder recipeIngredientsFinder;

    @Transactional
    public UUID create(UUID recipeId, CaptionInfo captionInfo) {
        String videoId = recipeFinder.findVideoId(recipeId);
        List<Ingredient> ingredients = recipeIngredientsClient
                .fetchRecipeIngredients(videoId, captionInfo);
        RecipeIngredients recipeIngredients = RecipeIngredients.from(ingredients, recipeId);
        recipeIngredientsCreator.create(recipeIngredients);
        return recipeIngredients.getId();
    }

    public IngredientsInfo getIngredientsInfoOfRecipe(UUID recipeId) {
        RecipeIngredients recipeIngredients = recipeIngredientsFinder.findByRecipeId(recipeId);
        return IngredientsInfo.from(recipeIngredients);
    }
}
