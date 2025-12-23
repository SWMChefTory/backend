package com.cheftory.api.recipe.challenge.exception;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.recipe.exception.RecipeException;

@PocOnly(until = "2025-12-31")
public class RecipeChallengeException extends RecipeException {

  public RecipeChallengeException(RecipeChallengeErrorCode errorCode) {
    super(errorCode);
  }
}
