package com.cheftory.api.recipe.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.Error;

public class RecipeException extends CheftoryException {

    public RecipeException(Error error) {
        super(error);
    }

    public RecipeException(Error error, Throwable cause) {
        super(error, cause);
    }
}
