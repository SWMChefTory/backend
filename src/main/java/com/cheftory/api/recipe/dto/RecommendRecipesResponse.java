package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecommendRecipesResponse(
        @JsonProperty("recommend_recipes") List<RecipeOverviewResponse> recommendRecipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {
    public static RecommendRecipesResponse from(CursorPage<RecipeOverview> recipes) {
        List<RecipeOverviewResponse> responses =
                recipes.items().stream().map(RecipeOverviewResponse::of).toList();

        return new RecommendRecipesResponse(responses, recipes.hasNext(), recipes.nextCursor());
    }
}
