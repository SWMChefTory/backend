package com.cheftory.api.recipe.content.youtubemeta.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class YoutubeMetaException extends RecipeException {

    public YoutubeMetaException(YoutubeMetaErrorCode errorCode) {
        super(errorCode);
    }
}
