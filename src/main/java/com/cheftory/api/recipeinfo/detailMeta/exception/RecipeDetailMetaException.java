package com.cheftory.api.recipeinfo.detailMeta.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeDetailMetaException extends RecipeInfoException {

  public RecipeDetailMetaException(RecipeDetailMetaErrorCode e) {
    super(e);
  }
}
