package com.cheftory.api.recipeinfo.detailMeta.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum RecipeDetailMetaErrorCode implements ErrorMessage {
  DETAIL_META_NOT_FOUND("RECIPE_DETAIL_META_001", "레시피 브리핑 생성에 실패했습니다."),
  ;
  final String errorCode;
  final String message;

  RecipeDetailMetaErrorCode(String errorCode, String message) {
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
