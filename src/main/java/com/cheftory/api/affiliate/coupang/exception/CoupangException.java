package com.cheftory.api.affiliate.coupang.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class CoupangException extends RecipeInfoException {

  public CoupangException(CoupangErrorCode e) {
    super(e);
  }
}
