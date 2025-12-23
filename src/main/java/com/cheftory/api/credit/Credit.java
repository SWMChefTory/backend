package com.cheftory.api.credit;

import java.util.UUID;

public record Credit(UUID userId, long amount, CreditReason reason, String idempotencyKey) {
  public Credit {
    if (userId == null) throw new IllegalArgumentException("userId is required");
    if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
    if (reason == null) throw new IllegalArgumentException("reason is required");
    if (idempotencyKey == null || idempotencyKey.isBlank())
      throw new IllegalArgumentException("idempotencyKey is required");
  }

  public static Credit signupBonus(UUID userId) {
    return new Credit(userId, 100L, CreditReason.SIGNUP_BONUS, "signup-bonus:" + userId);
  }

  public static Credit recipeCreate(UUID userId, UUID recipeId, long cost) {
    return new Credit(
        userId, cost, CreditReason.RECIPE_CREATE, "recipe-create:" + userId + ":" + recipeId);
  }

  public static Credit recipeCreateRefund(UUID userId, UUID recipeId, long amount) {
    return new Credit(
        userId,
        amount,
        CreditReason.RECIPE_CREATE_REFUND,
        "recipe-create-refund:" + userId + ":" + recipeId);
  }

  public void grantTo(CreditUserBalance balance) {
    balance.apply(amount);
  }

  public void spendFrom(CreditUserBalance balance) {
    balance.apply(-amount);
  }
}
