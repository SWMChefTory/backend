package com.cheftory.api.recipe.exception;

import com.cheftory.api.exception.CheftoryException;

public class RecipeException extends CheftoryException {

    public RecipeException(RecipeErrorCode errorCode) {
      super(errorCode);
    }
}