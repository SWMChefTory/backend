package com.cheftory.api.recipe.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeOverviewResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record SearchedRecipesResponse(
        @JsonProperty("searched_recipes") List<RecipeOverviewResponse> recipes,
        @JsonProperty("current_page") Integer currentPage,
        @JsonProperty("total_pages") Integer totalPages,
        @JsonProperty("total_elements") Long totalElements,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {
    @Deprecated(forRemoval = true)
    public static SearchedRecipesResponse from(Page<RecipeOverview> page) {
        List<RecipeOverviewResponse> items =
                page.stream().map(RecipeOverviewResponse::of).toList();
        return new SearchedRecipesResponse(
                items, page.getNumber(), page.getTotalPages(), page.getTotalElements(), page.hasNext(), null);
    }

    public static SearchedRecipesResponse from(CursorPage<RecipeOverview> cursorPage) {
        List<RecipeOverviewResponse> items =
                cursorPage.items().stream().map(RecipeOverviewResponse::of).toList();
        return new SearchedRecipesResponse(items, null, null, null, cursorPage.hasNext(), cursorPage.nextCursor());
    }
}
