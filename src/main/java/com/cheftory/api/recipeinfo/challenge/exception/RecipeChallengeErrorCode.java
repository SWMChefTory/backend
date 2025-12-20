package com.cheftory.api.recipeinfo.challenge.exception;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.exception.ErrorMessage;

@PocOnly(until = "2025-12-31")
public enum RecipeChallengeErrorCode implements ErrorMessage {
  RECIPE_CHALLENGE_NOT_FOUND("RECIPE_CHALLENGE_001", "챌린지가 없습니다.");
  private final String errorCode;
  private final String message;

  RecipeChallengeErrorCode(String errorCode, String message) {
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
