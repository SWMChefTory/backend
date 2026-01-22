package com.cheftory.api.recipe.creation.credit;

import java.util.UUID;

public interface RecipeCreditPort {
    void spendRecipeCreate(UUID userId, UUID recipeId, long creditCost);

    void refundRecipeCreate(UUID userId, UUID recipeId, long creditCost);
}
