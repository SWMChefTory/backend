package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record RecipeCreateResponse(@JsonProperty("recipe_id") UUID recipeId) {
  public static RecipeCreateResponse from(UUID recipeId) {
    return new RecipeCreateResponse(recipeId);
  }
}
