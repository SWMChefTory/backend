package com.cheftory.api.recipe.ingredients.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class IngredientsFindResponse {
    private String content;
    public static IngredientsFindResponse from(String content){
        return IngredientsFindResponse.builder()
                .content(content)
                .build();
    }
}
