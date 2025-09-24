package com.cheftory.api.recipeinfo.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeInfoErrorCode implements ErrorMessage {
  RECIPE_INFO_NOT_FOUND("RECIPE_001", "레시피가 존재하지 않습니다."),
  RECIPE_BANNED("RECIPE_002", "접근할 수 없는 레시피 입니다."),
  RECIPE_NOT_COOK_VIDEO("RECIPE_003", "요리 비디오 id가 아닙니다."),
  RECIPE_CREATE_FAIL("RECIPE_004", "레시피 생성에 실패했습니다."),
  RECIPE_FAILED("RECIPE008", "실패한 레시피 입니다.");

  private final String errorCode;
  private final String message;

  RecipeInfoErrorCode(String errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
  }
}
