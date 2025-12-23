package com.cheftory.api.recipe.content.detail.client.dto;

import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClientRecipeDetailResponse(
    @JsonProperty("description") String description,
    @JsonProperty("ingredients") List<Ingredient> ingredients,
    @JsonProperty("tags") List<String> tags,
    @JsonProperty("servings") Integer servings,
    @JsonProperty("cook_time") Integer cookTime) {
  public record Ingredient(
      @JsonProperty("name") String name,
      @JsonProperty("amount") Integer amount,
      @JsonProperty("unit") String unit) {}

  public RecipeDetail toRecipeDetail() {
    List<RecipeDetail.Ingredient> recipeIngredients =
        this.ingredients.stream()
            .map(ing -> RecipeDetail.Ingredient.of(ing.name, ing.amount, ing.unit))
            .toList();
    return RecipeDetail.of(
        this.description, recipeIngredients, this.tags, this.servings, this.cookTime);
  }
}
