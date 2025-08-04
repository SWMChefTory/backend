package com.cheftory.api.recipe.viewstatus;

import com.cheftory.api.account.user.validator.ExistsUserId;
import com.cheftory.api.recipe.category.validator.ExistsRecipeCategoryId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecipeViewStatusRequest() {
  public record UpdateCategory(
      @JsonProperty("category_id")
      @NotNull
      @ExistsRecipeCategoryId
      UUID categoryId
  ) {}
}
