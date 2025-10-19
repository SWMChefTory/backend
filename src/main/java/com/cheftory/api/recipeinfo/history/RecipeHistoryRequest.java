package com.cheftory.api.recipeinfo.history;

import com.cheftory.api.recipeinfo.category.validator.ExistsRecipeCategoryId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecipeHistoryRequest() {
  public record UpdateCategory(
      @JsonProperty("category_id") @NotNull @ExistsRecipeCategoryId UUID categoryId) {}
}
