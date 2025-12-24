package com.cheftory.api.recipe.content.briefing.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum RecipeBriefingErrorCode implements ErrorMessage {
  BRIEFING_CREATE_FAIL("RECIPE_BRIEFING_001", "레시피 브리핑 생성에 실패했습니다."),
  ;
  final String errorCode;
  final String message;

  RecipeBriefingErrorCode(String errorCode, String message) {
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
