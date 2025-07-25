package com.cheftory.api.recipe.ingredients;

import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.ingredients.client.RecipeIngredientsClient;
import com.cheftory.api.recipe.ingredients.client.dto.ClientIngredientsResponse;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import com.cheftory.api.recipe.ingredients.exception.RecipeIngredientsErrorCode;
import com.cheftory.api.recipe.ingredients.exception.RecipeIngredientsException;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.ingredients.repository.RecipeIngredientsRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeIngredientsService {
    private final RecipeIngredientsClient recipeIngredientsClient;
    private final RecipeIngredientsRepository recipeIngredientsRepository;

    @Transactional
    public UUID create(UUID recipeId, String videoId, CaptionInfo captionInfo) {
        ClientIngredientsResponse response = recipeIngredientsClient
                .fetchRecipeIngredients(videoId, captionInfo);

        List<Ingredient> ingredients = response.getIngredients().stream()
            .map(ing->
                Ingredient.of(ing.getName(),ing.getAmount(),ing.getUnit()))
            .toList();
        RecipeIngredients recipeIngredients = RecipeIngredients.from(ingredients, recipeId);
        recipeIngredientsRepository.save(recipeIngredients);
        return recipeIngredients.getId();
    }

    public Optional<IngredientsInfo> findIngredientsInfoOfRecipe(UUID recipeId) {
        Optional<RecipeIngredients> optional =  recipeIngredientsRepository.findById(recipeId);
      return optional.map(IngredientsInfo::from);
    }

    public IngredientsInfo findIngredientsInfo(UUID ingredientId) {
        RecipeIngredients recipeIngredients = recipeIngredientsRepository.findByRecipeId(ingredientId);
        return IngredientsInfo.from(recipeIngredients);
    }
}
