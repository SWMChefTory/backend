package com.cheftory.api.recipe.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecipeCategoriesResponse(
    @JsonProperty("recipe_categories")
    List<RecipeCategoryResponse> recipeCategories
) {
  public static  RecipeCategoriesResponse from(
      List<CountRecipeCategory> categorizedRecipes
  ) {
    List<RecipeCategoryResponse> recipeCategories =
        categorizedRecipes.stream().map(RecipeCategoryResponse::from).toList();
    return new RecipeCategoriesResponse(recipeCategories);
  }

  public record RecipeCategoryResponse(
      String category,
      Integer count
  ) {
    public static RecipeCategoryResponse from(CountRecipeCategory countRecipeCategory) {
      return new RecipeCategoryResponse(
          countRecipeCategory.getCategory().getName(),
          countRecipeCategory.getCount()
      );
    }
  }
}
