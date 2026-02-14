package com.cheftory.api.recipe.challenge.exception;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 챌린지 관련 예외.
 *
 * <p>챌린지 조회, 참여 등의 작업 중 오류가 발생한 경우 던져집니다.</p>
 */
@PocOnly(until = "2025-12-31")
public class RecipeChallengeException extends RecipeException {

    public RecipeChallengeException(RecipeChallengeErrorCode errorCode) {
        super(errorCode);
    }

    public RecipeChallengeException(RecipeChallengeErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
