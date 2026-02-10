package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;

/**
 * 챌린지 타입.
 */
@PocOnly(until = "2025-12-31")
public enum ChallengeType {
    /** 단일 챌린지 */
    SINGLE,
    /** 주부 챌린지 */
    HOUSEWIFE
}
