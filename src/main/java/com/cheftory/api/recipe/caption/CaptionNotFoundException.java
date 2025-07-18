package com.cheftory.api.recipe.caption;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.MessageError;

public class CaptionNotFoundException extends CheftoryException {
    public CaptionNotFoundException (MessageError messageError) {
        super(messageError);
    }
}