package com.cheftory.api.recipeinfo.dto;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.recipeinfo.challenge.RecipeCompleteChallenge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

@PocOnly(until = "2025-12-31")
public record ChallengeRecipesResponse(
    @JsonProperty("complete_recipes") List<RecipeCompleteChallengeResponse> completeRecipes,
    @JsonProperty("challenge_recipes") List<RecipeOverviewResponse> challengeRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {

  public static ChallengeRecipesResponse from(
      List<RecipeCompleteChallenge> recipeCompleteChallenges, Page<RecipeOverview> recipes) {
    List<RecipeOverviewResponse> recipeOverviewResponses =
        recipes.stream().map(RecipeOverviewResponse::of).toList();
    List<RecipeCompleteChallengeResponse> recipeCompleteChallengeResponses =
        recipeCompleteChallenges.stream()
            .filter(RecipeCompleteChallenge::isFinished)
            .map(RecipeCompleteChallengeResponse::of)
            .toList();
    return new ChallengeRecipesResponse(
        recipeCompleteChallengeResponses,
        recipeOverviewResponses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext());
  }
}
