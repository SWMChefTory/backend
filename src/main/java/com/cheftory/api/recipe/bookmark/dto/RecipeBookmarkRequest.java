package com.cheftory.api.recipe.bookmark.dto;

import com.cheftory.api.recipe.category.validator.ExistsRecipeCategoryId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 레시피 북마크 요청 DTO
 */
public record RecipeBookmarkRequest() {
    /**
     * 카테고리 수정 요청
     */
    public record UpdateCategory(@JsonProperty("category_id") @NotNull @ExistsRecipeCategoryId UUID categoryId) {}
}
