package com.cheftory.api.recipeinfo.category.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum RecipeCategoryErrorCode implements ErrorMessage {
  RECIPE_CATEGORY_NOT_FOUND("RECIPE_CATEGORY_001", "해당 카테고리가 존재하지 않습니다."),
  RECIPE_CATEGORY_NAME_EMPTY("RECIPE_CATEGORY_002", "카테고리 이름은 필수입니다.");
  private final String errorCode;
  private final String message;

  RecipeCategoryErrorCode(String errorCode, String message) {
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
