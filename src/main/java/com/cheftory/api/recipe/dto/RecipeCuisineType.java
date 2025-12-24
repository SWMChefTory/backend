package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.Getter;

@Getter
public enum RecipeCuisineType {
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

  RecipeCuisineType(String koreanName) {
    this.koreanName = koreanName;
  }

  public static RecipeCuisineType fromString(String type) {
    try {
      return RecipeCuisineType.valueOf(type.toUpperCase());
    } catch (Exception e) {
      throw new RecipeException(RecipeErrorCode.INVALID_CUISINE_TYPE);
    }
  }
}
