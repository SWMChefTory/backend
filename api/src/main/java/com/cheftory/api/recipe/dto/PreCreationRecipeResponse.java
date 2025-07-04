package com.cheftory.api.recipe.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
public class PreCreationRecipeResponse {
    private final UUID recipeId;
    public static PreCreationRecipeResponse of(UUID recipeId) {
        return PreCreationRecipeResponse.builder()
                .recipeId(recipeId)
                .build();
    }
}
