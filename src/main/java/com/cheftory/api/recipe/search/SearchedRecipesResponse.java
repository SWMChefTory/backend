package com.cheftory.api.recipe.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeOverviewResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 검색된 레시피 목록 응답 DTO
 *
 * @param recipes 검색된 레시피 개요 목록
 * @param hasNext 다음 페이지 존재 여부
 * @param nextCursor 다음 페이지 커서
 */
public record SearchedRecipesResponse(
        @JsonProperty("searched_recipes") List<RecipeOverviewResponse> recipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    /**
     * CursorPage로부터 응답 DTO 생성
     *
     * @param cursorPage 레시피 개요 커서 페이지
     * @return 검색된 레시피 목록 응답 DTO
     */
    public static SearchedRecipesResponse from(CursorPage<RecipeOverview> cursorPage) {
        List<RecipeOverviewResponse> items =
                cursorPage.items().stream().map(RecipeOverviewResponse::of).toList();
        return new SearchedRecipesResponse(items, cursorPage.hasNext(), cursorPage.nextCursor());
    }
}
