package com.cheftory.api.recipeaccess.dto;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
public class RecipeAccessResponse {
  private final UUID recipeViewStateId;

  public static RecipeAccessResponse from(UUID recipeViewStateId) {
    return RecipeAccessResponse.builder().recipeViewStateId(recipeViewStateId).build();
  }
}
