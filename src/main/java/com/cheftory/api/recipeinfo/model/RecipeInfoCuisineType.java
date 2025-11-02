package com.cheftory.api.recipeinfo.model;

public enum RecipeInfoCuisineType {
  TRENDING,
  CHEF,
  KOREAN,
  SNACK,
  CHINESE,
  JAPANESE,
  WESTERN,
  DESSERT,
  HEALTHY,
  BABY,
  SIMPLE;

  public static RecipeInfoCuisineType fromString(String type) {
    try {
      String upperCase = type.toUpperCase();
      return RecipeInfoCuisineType.valueOf(upperCase);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid cuisine type: " + type);
    }
  }
}
