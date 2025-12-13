package com.cheftory.api.recipeinfo.dto;

import com.cheftory.api.recipeinfo.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;
import lombok.Getter;

@Getter
public enum RecipeInfoCuisineType {
  KOREAN("한식"),
  SNACK("분식"),
  CHINESE("중식"),
  JAPANESE("일식"),
  WESTERN("양식"),
  DESSERT("디저트"),
  HEALTHY("건강식"),
  BABY("유아식"),
  SIMPLE("간편식");

  private final String koreanName;

  RecipeInfoCuisineType(String koreanName) {
    this.koreanName = koreanName;
  }

  public static RecipeInfoCuisineType fromString(String type) {
    try {
      return RecipeInfoCuisineType.valueOf(type.toUpperCase());
    } catch (Exception e) {
      throw new RecipeInfoException(RecipeInfoErrorCode.INVALID_CUISINE_TYPE);
    }
  }
}
