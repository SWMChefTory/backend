package com.cheftory.api.recipeinfo.detail.entity;

import java.util.List;

public record RecipeDetail(
    String description,
    List<Ingredient> ingredients,
    List<String> tags,
    Integer servings,
    Integer cookTime) {
  public record Ingredient(String name, Integer amount, String unit) {
    public static Ingredient of(String name, Integer amount, String unit) {
      return new Ingredient(name, amount, unit);
    }
  }

  public static RecipeDetail of(
      String description,
      List<Ingredient> ingredients,
      List<String> tags,
      Integer servings,
      Integer cookTime) {
    return new RecipeDetail(description, ingredients, tags, servings, cookTime);
  }
}
