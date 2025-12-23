package com.cheftory.api.credit;

import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditAdapter implements RecipeCreditPort {
  private final CreditService creditService;

  @Override
  public void spendRecipeCreate(UUID userId, UUID recipeId, long creditCost) {
    creditService.grant(Credit.recipeCreate(userId, recipeId, creditCost));
  }

  @Override
  public void refundRecipeCreate(UUID userId, UUID recipeId, long creditCost) {
    creditService.grant(Credit.recipeCreateRefund(userId, recipeId, creditCost));
  }
}
