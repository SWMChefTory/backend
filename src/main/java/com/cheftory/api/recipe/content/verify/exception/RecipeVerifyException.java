package com.cheftory.api.recipe.content.verify.exception;

import com.cheftory.api.exception.CheftoryException;

public class RecipeVerifyException extends CheftoryException {

    public RecipeVerifyException(RecipeVerifyErrorCode e) {
        super(e);
    }
}
