package com.cheftory.api.recipe.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecipeCategoryResponse() {
  public record Create(
      @JsonProperty("recipe_category_id")
      @NotNull
      UUID recipeCategoryId
  ) {
    public static Create from(UUID recipeCategoryId) {
      return new Create(recipeCategoryId);
    }
  }
}
