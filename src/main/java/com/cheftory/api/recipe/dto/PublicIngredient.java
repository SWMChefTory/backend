package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 공개 레시피 재료 DTO
 *
 * @param name 재료 이름
 * @param amount 양
 * @param unit 단위
 */
public record PublicIngredient(
        @JsonProperty("name") String name,
        @JsonProperty("amount") Integer amount,
        @JsonProperty("unit") String unit) {

    public static PublicIngredient from(RecipeIngredient ingredient) {
        return new PublicIngredient(ingredient.getName(), ingredient.getAmount(), ingredient.getUnit());
    }
}
