package com.cheftory.api.recipeinfo.search.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeSearchErrorCode implements ErrorMessage {
  RECIPE_SEARCH_FAILED("RECIPE_SEARCH_001", "레시피 검색에 실패 했습니다."),
  RECIPE_AUTOCOMPLETE_FAILED("RECIPE_SEARCH_002", "레시피 자동완성에 실패 했습니다.");
  final String errorCode;
  final String message;

  RecipeSearchErrorCode(String errorCode, String message) {
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
