package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CuisineRecipesResponse(
        @JsonProperty("cuisine_recipes") List<RecipeOverviewResponse> searchedRecipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    public static CuisineRecipesResponse from(CursorPage<RecipeOverview> recipes) {
        List<RecipeOverviewResponse> responses =
                recipes.items().stream().map(RecipeOverviewResponse::of).toList();

        return new CuisineRecipesResponse(responses, recipes.hasNext(), recipes.nextCursor());
    }
}
