package com.cheftory.api.recipe.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 레시피 카테고리 관련 요청 DTO
 */
public record RecipeCategoryRequest() {
    /**
     * 레시피 카테고리 생성 요청
     */
    public record Create(
            @JsonProperty("name") @NotNull @NotBlank String name) {}
}
