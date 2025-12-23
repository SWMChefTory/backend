package com.cheftory.api.affiliate.coupang.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class CoupangException extends RecipeException {

  public CoupangException(CoupangErrorCode e) {
    super(e);
  }
}
