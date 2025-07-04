package com.cheftory.api.recipe.ingredients;
import com.cheftory.api.recipe.ingredients.client.RecipeIngredientsClient;
import com.cheftory.api.recipe.ingredients.entity.Ingredients;
import com.cheftory.api.recipe.ingredients.repository.RecipeIngredientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRecipeIngredientsService {
    private final RecipeIngredientsRepository repository;
    private final RecipeIngredientsClient recipeIngredientsClient;

    public UUID create(UUID recipeInfoId, String videoId, String captionContent) {
        String content = recipeIngredientsClient.fetchRecipeIngredients(videoId,captionContent);
        Ingredients ingredients = Ingredients.from(
                content, recipeInfoId
        );
        return repository
                .save(ingredients)
                .getId();
    }
}
