package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 챌린지 레시피 목록 응답 DTO (POC 전용)
 *
 * @param completeRecipes 완료한 챌린지 레시피 목록
 * @param challengeRecipes 진행 중인 챌린지 레시피 목록
 * @param hasNext 다음 페이지 존재 여부
 * @param nextCursor 다음 페이지 커서
 */
@PocOnly(until = "2025-12-31")
public record ChallengeRecipesResponse(
        @JsonProperty("complete_recipes") List<RecipeCompleteChallengeResponse> completeRecipes,
        @JsonProperty("challenge_recipes") List<RecipeOverviewResponse> challengeRecipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    /**
     * 완료 챌린지와 레시피 목록으로부터 응답 DTO 생성
     *
     * @param recipeCompleteChallenges 완료 챌린지 목록
     * @param recipes 레시피 개요 커서 페이지
     * @return 챌린지 레시피 목록 응답 DTO
     */
    public static ChallengeRecipesResponse from(
            List<RecipeCompleteChallenge> recipeCompleteChallenges, CursorPage<RecipeOverview> recipes) {
        List<RecipeOverviewResponse> recipeOverviewResponses =
                recipes.items().stream().map(RecipeOverviewResponse::of).toList();
        List<RecipeCompleteChallengeResponse> recipeCompleteChallengeResponses = recipeCompleteChallenges.stream()
                .filter(RecipeCompleteChallenge::isFinished)
                .map(RecipeCompleteChallengeResponse::of)
                .toList();
        return new ChallengeRecipesResponse(
                recipeCompleteChallengeResponses, recipeOverviewResponses, recipes.hasNext(), recipes.nextCursor());
    }
}
