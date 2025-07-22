package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
public class RecipeCreateResponse {

  @JsonProperty("recipe_id")
  private final UUID recipeId;

  public static RecipeCreateResponse from(UUID recipeId) {
    return RecipeCreateResponse.builder().recipeId(recipeId).build();
  }
}
