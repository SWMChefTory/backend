package com.cheftory.api.recipe.caption;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.recipe.caption.errorcode.CaptionErrorCode;

public class CaptionException extends CheftoryException {
    public CaptionException(CaptionErrorCode errorCode) {
        super(errorCode);
    }
}