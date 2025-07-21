package com.cheftory.api.recipeviewstate.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum RecipeViewStateErrorCode implements ErrorMessage {
  RECIPE_VIEW_STATE_NOT_FOUND("RECIPE_VIEW_STATE_001","레시피 시청 상태가 존재하지 않습니다.");
  private final String errorCode;
  private final String message;

  RecipeViewStateErrorCode(String errorCode, String message) {
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
