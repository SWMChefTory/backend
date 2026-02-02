package com.cheftory.api.recipe.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeOverviewResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SearchedRecipesResponse(
        @JsonProperty("searched_recipes") List<RecipeOverviewResponse> recipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {
    public static SearchedRecipesResponse from(CursorPage<RecipeOverview> cursorPage) {
        List<RecipeOverviewResponse> items =
                cursorPage.items().stream().map(RecipeOverviewResponse::of).toList();
        return new SearchedRecipesResponse(items, cursorPage.hasNext(), cursorPage.nextCursor());
    }
}
