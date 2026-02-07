package com.cheftory.api.credit.entity;

import com.cheftory.api._common.Clock;

import java.util.UUID;

public record Credit(UUID userId, long amount, CreditReason reason, String idempotencyKey) {

    public static Credit signupBonus(UUID userId) {
        return new Credit(userId, 100L, CreditReason.SIGNUP_BONUS, "signup-bonus:" + userId);
    }

    public static Credit recipeCreate(UUID userId, UUID recipeId, long cost) {
        return new Credit(userId, cost, CreditReason.RECIPE_CREATE, "recipe-create:" + userId + ":" + recipeId);
    }

    public static Credit recipeCreateRefund(UUID userId, UUID recipeId, long amount) {
        return new Credit(
                userId, amount, CreditReason.RECIPE_CREATE_REFUND, "recipe-create-refund:" + userId + ":" + recipeId);
    }

    public static Credit tutorial(UUID userId) {
        return new Credit(userId, 30L, CreditReason.TUTORIAL, "tutorial:" + userId);
    }

    public static Credit share(UUID userId, int count, Clock clock) {
        return new Credit(userId, 10L, CreditReason.SHARE, "share:" + userId + ":" + clock.now().toLocalDate().toString() + ":" + count);
    }

    public void grantTo(CreditUserBalance balance) {
        balance.apply(amount);
    }

    public void spendFrom(CreditUserBalance balance) {
        balance.apply(-amount);
    }
}
