package com.cheftory.api.recipeinfo.model;

public enum RecipeInfoRecommendType {
  POPULAR,
  TRENDING,
  CHEF;

  public static RecipeInfoRecommendType fromString(String type) {
    try {
      return RecipeInfoRecommendType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid recommend type: " + type);
    }
  }
}
