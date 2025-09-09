package com.cheftory.api.recipe.step.client.dto;

import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record ClientRecipeStepsRequest(
    @JsonProperty("video_id")
    String videoId,
    @JsonProperty("video_type")
    String videoType,
    @JsonProperty("captions_data")
    RecipeCaption recipeCaption,
    @JsonProperty("ingredients")
    List<Ingredient> ingredients
) {
    private record Ingredient(
        @JsonProperty("name")
        String name,
        @JsonProperty("amount")
        Integer  amount,
        @JsonProperty("unit")
        String unit
    ) {
        public static Ingredient from(RecipeAnalysis.Ingredient ingredient) {
            return new Ingredient(
                ingredient.getName(),
                ingredient.getAmount(),
                ingredient.getUnit()
            );
        }
    }
    public static ClientRecipeStepsRequest from(String videoId, String videoType, RecipeCaption recipeCaption, List<RecipeAnalysis.Ingredient> ingredients) {
        return new ClientRecipeStepsRequest(
            videoId,
            videoType,
            recipeCaption,
            ingredients.stream()
                .map(Ingredient::from)
                .toList()
        );
    }
}
