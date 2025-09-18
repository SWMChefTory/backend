package com.cheftory.api.recipe.category.exception;

import com.cheftory.api.exception.CheftoryException;

public class RecipeCategoryException extends CheftoryException {

  public RecipeCategoryException(RecipeCategoryErrorCode errorCode) {
    super(errorCode);
  }
}
