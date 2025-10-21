package com.cheftory.api.recipeinfo.youtubemeta.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum YoutubeMetaErrorCode implements ErrorMessage {
  YOUTUBE_META_NOT_FOUND("YOUTUBE_META_001", "유튜브 메타 정보가 존재하지 않습니다."),
  YOUTUBE_URL_HOST_NULL("YOUTUBE_META_003", "유튜브 URL의 호스트가 비어있습니다."),
  YOUTUBE_URL_HOST_INVALID("YOUTUBE_META_004", "지원하지 않는 유튜브 도메인입니다."),
  YOUTUBE_URL_PATH_NULL("YOUTUBE_META_005", "유튜브 URL의 경로가 비어있습니다."),
  YOUTUBE_URL_INVALID("YOUTUBE_META_006", "유효하지 않은 유튜브 URL 형식입니다."),
  YOUTUBE_URL_QUERY_PARAM_INVALID("YOUTUBE_META_007", "유튜브 URL의 쿼리 파라미터가 유효하지 않습니다."),
  YOUTUBE_META_DUPLICATED("YOUTUBE_META_008", "이미 존재하는 유튜브 메타 정보입니다."),
  YOUTUBE_URL_PATH_INVALID("YOUTUBE_META_009", "유튜브 URL의 경로가 유효하지 않습니다."),
  YOUTUBE_META_BANNED("YOUTUBE_META_010", "요리 비디오 URL이 아닙니다."),
  YOUTUBE_META_BLOCKED("YOUTUBE_META_011", "유튜브 메타 정보가 차단되었습니다."),
  YOUTUBE_META_NOT_BLOCKED_VIDEO("YOUTUBE_META_012", "차단되지 않은 유튜브 메타 정보입니다.");

  private final String errorCode;
  private final String message;

  YoutubeMetaErrorCode(String errorCode, String message) {
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
