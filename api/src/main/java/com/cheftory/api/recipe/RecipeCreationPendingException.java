package com.cheftory.api.recipe;

import lombok.Getter;

@Getter
public class RecipeCreationPendingException extends RuntimeException {
    private final RecipeCreationState state;

    public RecipeCreationPendingException(RecipeCreationState state) {
        super("해당 레시피는 현재 생성중입니다. 현재 생성 단계 : " + state);
        this.state = state;
    }
}
