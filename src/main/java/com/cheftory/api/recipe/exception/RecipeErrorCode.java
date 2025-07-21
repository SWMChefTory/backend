package com.cheftory.api.recipe.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeErrorCode implements ErrorMessage {
  RECIPE_NOT_FOUND("RECIPE_001","레시피가 존재하지 않습니다."),
  RECIPE_BANNED("RECIPE002","접근할 수 없는 레시피 입니다.");

  private final String errorCode;
  private final String message;

  RecipeErrorCode(String errorCode, String message){
    this.errorCode = errorCode;
    this.message = message;
  }
}
