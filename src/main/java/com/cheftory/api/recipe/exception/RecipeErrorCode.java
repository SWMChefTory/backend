package com.cheftory.api.recipe.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeErrorCode implements ErrorMessage {
  RECIPE_NOT_FOUND("RECIPE_001","레시피가 존재하지 않습니다."),
  RECIPE_BANNED("RECIPE_002","접근할 수 없는 레시피 입니다."),

  YOUTUBE_URL_HOST_INVALID("RECIPE003", "기대하는 호스트가 아닙니다."),
  YOUTUBE_URL_QUERY_PARAM_INVALID("RECIPE004", "기대하는 호스트가 아닙니다."),
  YOUTUBE_URL_NULL("RECIPE005", "URL이 비어있습니다."),
  YOUTUBE_URL_HOST_NULL("RECIPE006", "호스트가 비어있습니다."),
  YOUTUBE_URL_PATH_NULL("RECIPE007", "경로가 비어있습니다."),
  YOUTUBE_URL_INVALID("RECIPE009", "유효하지 않은 유튜브 URL입니다."),

  RECIPE_FAILED("RECIPE008", "실패한 레시피 입니다.");


  private final String errorCode;
  private final String message;

  RecipeErrorCode(String errorCode, String message){
    this.errorCode = errorCode;
    this.message = message;
  }
}
