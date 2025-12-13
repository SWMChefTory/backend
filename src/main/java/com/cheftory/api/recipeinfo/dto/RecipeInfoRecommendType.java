package com.cheftory.api.recipeinfo.dto;

import com.cheftory.api.recipeinfo.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public enum RecipeInfoRecommendType {
  POPULAR,
  TRENDING,
  CHEF;

  public static RecipeInfoRecommendType fromString(String type) {
    try {
      return RecipeInfoRecommendType.valueOf(type.toUpperCase());
    } catch (Exception e) {
      throw new RecipeInfoException(RecipeInfoErrorCode.INVALID_RECOMMEND_TYPE);
    }
  }
}
