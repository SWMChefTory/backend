package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.RecipeCreationState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
public class RecipeFindPendingResponse {
    private final RecipeCreationState recipeCreationState;
    public static RecipeFindPendingResponse from(RecipeCreationState recipeCreationState){
        return RecipeFindPendingResponse.builder()
                .recipeCreationState(recipeCreationState)
                .build();
    }
}
