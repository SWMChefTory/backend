package com.cheftory.api.recipeinfo.recipe.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeErrorCode implements ErrorMessage {
  RECIPE_NOT_FOUND("RECIPE_001", "레시피가 존재하지 않습니다."),
  RECIPE_BANNED("RECIPE_002", "접근할 수 없는 레시피 입니다."),
  RECIPE_FAILED("RECIPE_003", "실패한 레시피 입니다."),
  RECIPE_NOT_COOK_VIDEO("RECIPE_004", "요리 비디오 id가 아닙니다."),
  RECIPE_NOT_VALID_QUERY("RECIPE_005", "유효하지 않은 레시피 조회 쿼리입니다.");

  final String errorCode;
  final String message;

  RecipeErrorCode(String errorCode, String message) {
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
