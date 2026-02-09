package com.cheftory.api.credit;

import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreditAdapter implements RecipeCreditPort {
    private final CreditService creditService;

    @Override
    public void spendRecipeCreate(UUID userId, UUID recipeId, long creditCost) throws CreditException {
        creditService.spend(Credit.recipeCreate(userId, recipeId, creditCost));
    }

    @Override
    public void refundRecipeCreate(UUID userId, UUID recipeId, long creditCost) throws CreditException {
        creditService.grant(Credit.recipeCreateRefund(userId, recipeId, creditCost));
    }
}
