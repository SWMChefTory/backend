package com.cheftory.api.recipe.creation.credit;

import com.cheftory.api.credit.exception.CreditException;
import java.util.UUID;

public interface RecipeCreditPort {
    void spendRecipeCreate(UUID userId, UUID recipeId, long creditCost) throws CreditException;

    void refundRecipeCreate(UUID userId, UUID recipeId, long creditCost) throws CreditException;
}
