package com.cheftory.api.recipe.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;

public class RecipeException extends CheftoryException {

    public RecipeException(ErrorMessage errorCode) {
        super(errorCode);
    }
}
