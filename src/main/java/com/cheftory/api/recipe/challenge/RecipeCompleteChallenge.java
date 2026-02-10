package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레시피 챌린지 완료 여부.
 *
 * <p>레시피가 특정 챌린지를 완료했는지 여부를 나타냅니다.</p>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@PocOnly(until = "2025-12-31")
public class RecipeCompleteChallenge {

    private UUID recipeId;
    private boolean isFinished;

    /**
     * 레시피 챌린지 완료 정보를 생성합니다.
     *
     * @param recipeId 레시피 ID
     * @param isFinished 완료 여부
     * @return 레시피 챌린지 완료 정보
     */
    public static RecipeCompleteChallenge of(UUID recipeId, boolean isFinished) {
        return new RecipeCompleteChallenge(recipeId, isFinished);
    }
}
