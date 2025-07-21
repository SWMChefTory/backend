package com.cheftory.api.recipe.caption.exception;

import com.cheftory.api.exception.CheftoryException;

public class RecipeCaptionException extends CheftoryException {
    public RecipeCaptionException(CaptionErrorCode errorCode) {
        super(errorCode);
    }
}