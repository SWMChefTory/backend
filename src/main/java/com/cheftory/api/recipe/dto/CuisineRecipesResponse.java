package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 요리 타입별 레시피 목록 응답 DTO
 *
 * @param cuisineRecipes 레시피 개요 목록
 * @param hasNext 다음 페이지 존재 여부
 * @param nextCursor 다음 페이지 커서
 */
public record CuisineRecipesResponse(
        @JsonProperty("cuisine_recipes") List<RecipeOverviewResponse> searchedRecipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    /**
     * CursorPage로부터 응답 DTO 생성
     *
     * @param recipes 레시피 개요 커서 페이지
     * @return 요리 타입별 레시피 목록 응답 DTO
     */
    public static CuisineRecipesResponse from(CursorPage<RecipeOverview> recipes) {
        List<RecipeOverviewResponse> responses =
                recipes.items().stream().map(RecipeOverviewResponse::of).toList();

        return new CuisineRecipesResponse(responses, recipes.hasNext(), recipes.nextCursor());
    }
}
