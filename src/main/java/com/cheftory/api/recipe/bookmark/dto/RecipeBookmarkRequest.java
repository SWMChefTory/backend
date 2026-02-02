package com.cheftory.api.recipe.bookmark.dto;

import com.cheftory.api.recipe.category.validator.ExistsRecipeCategoryId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecipeBookmarkRequest() {
    public record UpdateCategory(@JsonProperty("category_id") @NotNull @ExistsRecipeCategoryId UUID categoryId) {}
}
