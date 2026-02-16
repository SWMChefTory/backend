package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 레시피 챌린지 완료 응답 DTO (POC 전용)
 *
 * @param recipeId 레시피 ID
 */
@PocOnly(until = "2025-12-31")
public record RecipeCompleteChallengeResponse(
        @JsonProperty("recipe_id") String recipeId) {

    /**
     * RecipeCompleteChallenge 엔티티로부터 응답 DTO 생성
     *
     * @param recipeCompleteChallenge 레시피 챌린지 완료 엔티티
     * @return 레시피 챌린지 완료 응답 DTO
     */
    public static RecipeCompleteChallengeResponse of(RecipeCompleteChallenge recipeCompleteChallenge) {
        return new RecipeCompleteChallengeResponse(
                recipeCompleteChallenge.getRecipeId().toString());
    }
}
