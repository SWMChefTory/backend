package com.cheftory.api.recipe.content.youtubemeta.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 유튜브 메타 관련 에러 코드
 */
public enum YoutubeMetaErrorCode implements Error {
    /**
     * 유튜브 메타 정보가 존재하지 않음
     */
    YOUTUBE_META_NOT_FOUND("YOUTUBE_META_001", "유튜브 메타 정보가 존재하지 않습니다.", ErrorType.NOT_FOUND),
    /**
     * 유튜브 URL의 호스트가 비어있음
     */
    YOUTUBE_URL_HOST_NULL("YOUTUBE_META_003", "유튜브 URL의 호스트가 비어있습니다.", ErrorType.VALIDATION),
    /**
     * 지원하지 않는 유튜브 도메인
     */
    YOUTUBE_URL_HOST_INVALID("YOUTUBE_META_004", "지원하지 않는 유튜브 도메인입니다.", ErrorType.VALIDATION),
    /**
     * 유튜브 URL의 경로가 비어있음
     */
    YOUTUBE_URL_PATH_NULL("YOUTUBE_META_005", "유튜브 URL의 경로가 비어있습니다.", ErrorType.VALIDATION),
    /**
     * 유튜브 URL의 쿼리 파라미터가 유효하지 않음
     */
    YOUTUBE_URL_QUERY_PARAM_INVALID("YOUTUBE_META_007", "유튜브 URL의 쿼리 파라미터가 유효하지 않습니다.", ErrorType.VALIDATION),
    /**
     * 유튜브 URL의 경로가 유효하지 않음
     */
    YOUTUBE_URL_PATH_INVALID("YOUTUBE_META_009", "유튜브 URL의 경로가 유효하지 않습니다.", ErrorType.VALIDATION),
    /**
     * 동영상을 찾을 수 없음
     */
    YOUTUBE_META_VIDEO_NOT_FOUND("YOUTUBE_META_013", "동영상을 찾을 수 없습니다.", ErrorType.NOT_FOUND),
    /**
     * 동영상 길이 정보를 찾을 수 없음
     */
    YOUTUBE_META_VIDEO_DURATION_NOT_FOUND("YOUTUBE_META_014", "동영상 길이 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND),
    /**
     * 임베드할 수 없는 동영상
     */
    YOUTUBE_META_VIDEO_NOT_EMBEDDABLE("YOUTUBE_META_016", "임베드할 수 없는 동영상입니다.", ErrorType.VALIDATION),
    /**
     * 유튜브 메타 정보 API 호출 중 오류 발생
     */
    YOUTUBE_META_API_ERROR("YOUTUBE_META_017", "유튜브 메타 정보 API 호출 중 오류가 발생했습니다.", ErrorType.INTERNAL),
    /**
     * 썸네일 정보를 찾을 수 없음
     */
    YOUTUBE_META_THUMBNAIL_NOT_FOUND("YOUTUBE_META_018", "썸네일 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND),
    ;

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    YoutubeMetaErrorCode(String errorCode, String message, ErrorType type) {
        this.errorCode = errorCode;
        this.message = message;
        this.type = type;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ErrorType getType() {
        return type;
    }
}
