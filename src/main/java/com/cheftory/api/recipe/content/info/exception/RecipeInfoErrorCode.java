package com.cheftory.api.recipe.content.info.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeInfoErrorCode implements ErrorMessage {
  RECIPE_INFO_NOT_FOUND("RECIPE_INFO_001", "레시피가 존재하지 않습니다."),
  RECIPE_BANNED("RECIPE_INFO_002", "접근할 수 없는 레시피 입니다."),
  RECIPE_FAILED("RECIPE_INFO_003", "실패한 레시피 입니다."),
  RECIPE_NOT_COOK_VIDEO("RECIPE_INFO_004", "요리 비디오 id가 아닙니다."),
  RECIPE_NOT_VALID_QUERY("RECIPE_INFO_005", "유효하지 않은 레시피 조회 쿼리입니다.");

  final String errorCode;
  final String message;

  RecipeInfoErrorCode(String errorCode, String message) {
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
