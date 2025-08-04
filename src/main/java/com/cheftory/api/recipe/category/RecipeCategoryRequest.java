package com.cheftory.api.recipe.watched.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecipeCategoryRequest() {
  public record Create(
      @JsonProperty("name")
      @NotNull
      @NotBlank
      String name
  ) {}

  public record Delete(
      @JsonProperty("recipe_category_id")
      UUID recipeCategoryId
  ) {}
}
