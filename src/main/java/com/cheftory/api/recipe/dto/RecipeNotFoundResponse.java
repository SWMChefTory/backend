package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.helper.repository.RecipeNotFoundException;
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
