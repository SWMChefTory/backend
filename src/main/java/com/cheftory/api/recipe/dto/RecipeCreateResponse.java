package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * 레시피 생성 응답 DTO.
 *
 * @param recipeId 생성된 레시피 ID
 */
public record RecipeCreateResponse(@JsonProperty("recipe_id") UUID recipeId) {
    /**
     * 레시피 ID로부터 응답을 생성합니다.
     *
     * @param recipeId 레시피 ID
     * @return 레시피 생성 응답
     */
    public static RecipeCreateResponse from(UUID recipeId) {
        return new RecipeCreateResponse(recipeId);
    }
}
