package com.cheftory.api.recipe.analysis.client.dto;

import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record ClientRecipeAnalysisResponse(
    @JsonProperty("description")
    String description,
    @JsonProperty("ingredients")
    List<Ingredient> ingredients,
    @JsonProperty("tags")
    List<String> tags,
    @JsonProperty("servings")
    Integer servings,
    @JsonProperty("cook_time")
    Integer cookTime
) {
    private record Ingredient(
        @JsonProperty("name")
        String name,
        @JsonProperty("amount")
        Integer amount,
        @JsonProperty("unit")
        String unit
    ) {}

    public List<RecipeAnalysis.Ingredient> toIngredients() {
        return ingredients.stream()
            .map(ingredient -> new RecipeAnalysis.Ingredient(
                ingredient.name,
                ingredient.amount,
                ingredient.unit
            ))
            .toList();
    }
}
