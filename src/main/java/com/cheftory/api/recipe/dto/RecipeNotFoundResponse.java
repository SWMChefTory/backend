package com.cheftory.api.recipe.dto;

import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RecipeNotFoundResponse {
    String message;

    public static RecipeNotFoundResponse from(String message) {
        return RecipeNotFoundResponse.builder()
                .message(message)
                .build();
    }
}
