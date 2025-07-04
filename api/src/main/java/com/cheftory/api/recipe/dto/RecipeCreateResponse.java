package com.cheftory.api.recipe.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RecipeCreateResponse {
    private UUID recipeInfoId;
    public static RecipeCreateResponse from(UUID recipeInfoId) {
        return  RecipeCreateResponse.builder()
                .recipeInfoId(recipeInfoId)
                .build();
    }
}
