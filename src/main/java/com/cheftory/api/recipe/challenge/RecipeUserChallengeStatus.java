package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;

/**
 * 사용자 챌린지 상태.
 */
@PocOnly(until = "2025-12-31")
public enum RecipeUserChallengeStatus {
    /** 할당됨 */
    ASSIGNED,
    /** 완료됨 */
    COMPLETED,
}
