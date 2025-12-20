package com.cheftory.api.recipeinfo.challenge.exception;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

@PocOnly(until = "2025-12-31")
public class RecipeChallengeException extends RecipeInfoException {

  public RecipeChallengeException(RecipeChallengeErrorCode errorCode) {
    super(errorCode);
  }
}
