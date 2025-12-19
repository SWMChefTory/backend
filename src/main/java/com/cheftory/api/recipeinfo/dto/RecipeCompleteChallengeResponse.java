package com.cheftory.api.recipeinfo.dto;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.recipeinfo.challenge.RecipeCompleteChallenge;
import com.fasterxml.jackson.annotation.JsonProperty;

@PocOnly(until = "2025-12-31")
public record RecipeCompleteChallengeResponse(@JsonProperty("recipe_id") String recipeId) {
  public static RecipeCompleteChallengeResponse of(
      RecipeCompleteChallenge recipeCompleteChallenge) {
    return new RecipeCompleteChallengeResponse(recipeCompleteChallenge.getRecipeId().toString());
  }
}
