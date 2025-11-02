package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeErrorCode;

public enum RecipeInfoCuisineType {
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
    } catch (Exception e) {
      throw new RecipeInfoException(RecipeInfoErrorCode.INVALID_CUISINE_TYPE);
    }
  }
}
