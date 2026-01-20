package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record CuisineRecipesResponse(
        @JsonProperty("cuisine_recipes") List<RecipeOverviewResponse> searchedRecipes,
        @JsonProperty("current_page") Integer currentPage,
        @JsonProperty("total_pages") Integer totalPages,
        @JsonProperty("total_elements") Long totalElements,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    @Deprecated(forRemoval = true)
    public static CuisineRecipesResponse from(Page<RecipeOverview> recipes) {
        List<RecipeOverviewResponse> responses =
                recipes.stream().map(RecipeOverviewResponse::of).toList();

        return new CuisineRecipesResponse(
                responses,
                recipes.getNumber(),
                recipes.getTotalPages(),
                recipes.getTotalElements(),
                recipes.hasNext(),
                null);
    }

    public static CuisineRecipesResponse from(CursorPage<RecipeOverview> recipes) {
        List<RecipeOverviewResponse> responses =
                recipes.items().stream().map(RecipeOverviewResponse::of).toList();

        return new CuisineRecipesResponse(responses, null, null, null, recipes.hasNext(), recipes.nextCursor());
    }
}
