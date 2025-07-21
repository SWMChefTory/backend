package com.cheftory.api.recipe.ingredients.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum RecipeIngredientsErrorCode implements ErrorMessage {
  RECIPE_INGREDIENTS_NOT_FOUND("RECIPE_INGREDIENTS_001","레시피 재료가 존재하지 않습니다.");
  private final String errorCode;
  private final String message;

  RecipeIngredientsErrorCode(String errorCode, String message){
    this.errorCode = errorCode;
    this.message = message;
  }

  @Override
  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
