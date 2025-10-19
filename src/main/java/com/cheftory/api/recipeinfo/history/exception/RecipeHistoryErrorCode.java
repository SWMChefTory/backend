package com.cheftory.api.recipeinfo.history.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum RecipeHistoryErrorCode implements ErrorMessage {
  RECIPE_HISTORY_NOT_FOUND("RECIPE_HISTORY_001", "유저 레시피가 존재하지 않습니다."),
  RECIPE_HISTORY_ALREADY_EXISTS("RECIPE_HISTORY_002", "이미 존재하는 유저 레시피입니다.");
  private final String errorCode;
  private final String message;

  RecipeHistoryErrorCode(String errorCode, String message) {
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
