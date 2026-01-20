package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

@PocOnly(until = "2025-12-31")
public record ChallengeRecipesResponse(
        @JsonProperty("complete_recipes") List<RecipeCompleteChallengeResponse> completeRecipes,
        @JsonProperty("challenge_recipes") List<RecipeOverviewResponse> challengeRecipes,
        @JsonProperty("current_page") Integer currentPage,
        @JsonProperty("total_pages") Integer totalPages,
        @JsonProperty("total_elements") Long totalElements,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    @Deprecated(forRemoval = true)
    public static ChallengeRecipesResponse from(
            List<RecipeCompleteChallenge> recipeCompleteChallenges, Page<RecipeOverview> recipes) {
        List<RecipeOverviewResponse> recipeOverviewResponses =
                recipes.stream().map(RecipeOverviewResponse::of).toList();
        List<RecipeCompleteChallengeResponse> recipeCompleteChallengeResponses = recipeCompleteChallenges.stream()
                .filter(RecipeCompleteChallenge::isFinished)
                .map(RecipeCompleteChallengeResponse::of)
                .toList();
        return new ChallengeRecipesResponse(
                recipeCompleteChallengeResponses,
                recipeOverviewResponses,
                recipes.getNumber(),
                recipes.getTotalPages(),
                recipes.getTotalElements(),
                recipes.hasNext(),
                null);
    }

    public static ChallengeRecipesResponse from(
            List<RecipeCompleteChallenge> recipeCompleteChallenges, CursorPage<RecipeOverview> recipes) {
        List<RecipeOverviewResponse> recipeOverviewResponses =
                recipes.items().stream().map(RecipeOverviewResponse::of).toList();
        List<RecipeCompleteChallengeResponse> recipeCompleteChallengeResponses = recipeCompleteChallenges.stream()
                .filter(RecipeCompleteChallenge::isFinished)
                .map(RecipeCompleteChallengeResponse::of)
                .toList();
        return new ChallengeRecipesResponse(
                recipeCompleteChallengeResponses,
                recipeOverviewResponses,
                null,
                null,
                null,
                recipes.hasNext(),
                recipes.nextCursor());
    }
}
