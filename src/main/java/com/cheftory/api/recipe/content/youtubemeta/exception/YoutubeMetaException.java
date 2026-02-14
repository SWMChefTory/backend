package com.cheftory.api.recipe.content.youtubemeta.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 유튜브 메타 예외
 */
public class YoutubeMetaException extends RecipeException {

    /**
     * 유튜브 메타 예외 생성
     *
     * @param errorCode 에러 코드
     */
    public YoutubeMetaException(YoutubeMetaErrorCode errorCode) {
        super(errorCode);
    }

    public YoutubeMetaException(YoutubeMetaErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
