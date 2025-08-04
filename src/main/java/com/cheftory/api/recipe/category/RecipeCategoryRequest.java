package com.cheftory.api.recipe.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecipeCategoryRequest() {
  public record Create(
      @JsonProperty("name")
      @NotNull
      @NotBlank
      String name
  ) {}
}
