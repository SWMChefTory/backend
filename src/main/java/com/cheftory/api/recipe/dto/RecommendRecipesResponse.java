package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record RecommendRecipesResponse(
    @JsonProperty("recommend_recipes") List<RecipeOverviewResponse> recommendRecipes,
    @JsonProperty("current_page") Integer currentPage,
    @JsonProperty("total_pages") Integer totalPages,
    @JsonProperty("total_elements") Long totalElements,
    @JsonProperty("has_next") boolean hasNext,
    @JsonProperty("next_cursor") String nextCursor) {
  @Deprecated(forRemoval = true)
  public static RecommendRecipesResponse from(Page<RecipeOverview> recipes) {
    List<RecipeOverviewResponse> responses =
        recipes.stream().map(RecipeOverviewResponse::of).toList();

    return new RecommendRecipesResponse(
        responses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext(),
        null);
  }

  public static RecommendRecipesResponse from(CursorPage<RecipeOverview> recipes) {
    List<RecipeOverviewResponse> responses =
        recipes.items().stream().map(RecipeOverviewResponse::of).toList();

    return new RecommendRecipesResponse(
        responses, null, null, null, recipes.hasNext(), recipes.nextCursor());
  }
}
