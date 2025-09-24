package com.cheftory.api.recipeinfo.category.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeCategoryException extends RecipeInfoException {

  public RecipeCategoryException(RecipeCategoryErrorCode errorCode) {
    super(errorCode);
  }
}
