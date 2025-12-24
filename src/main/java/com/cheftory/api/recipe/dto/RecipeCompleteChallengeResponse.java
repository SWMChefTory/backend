package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.fasterxml.jackson.annotation.JsonProperty;

@PocOnly(until = "2025-12-31")
public record RecipeCompleteChallengeResponse(@JsonProperty("recipe_id") String recipeId) {
  public static RecipeCompleteChallengeResponse of(
      RecipeCompleteChallenge recipeCompleteChallenge) {
    return new RecipeCompleteChallengeResponse(recipeCompleteChallenge.getRecipeId().toString());
  }
}
