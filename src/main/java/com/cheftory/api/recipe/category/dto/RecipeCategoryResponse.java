package com.cheftory.api.recipe.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 레시피 카테고리 관련 응답 DTO
 */
public record RecipeCategoryResponse() {
    /**
     * 레시피 카테고리 생성 응답
     */
    public record Create(@JsonProperty("recipe_category_id") @NotNull UUID recipeCategoryId) {
        public static Create from(UUID recipeCategoryId) {
            return new Create(recipeCategoryId);
        }
    }
}
