package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 공개 레시피 목록 API 응답 래퍼
 *
 * @param data 공개 레시피 개요 목록
 * @param hasNext 다음 페이지 존재 여부
 * @param nextCursor 다음 페이지 커서
 */
public record PublicRecipesResponse(
        @JsonProperty("data") List<PublicRecipeOverview> data,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    public static PublicRecipesResponse from(CursorPage<PublicRecipeOverview> page) {
        return new PublicRecipesResponse(page.items(), page.hasNext(), page.nextCursor());
    }
}
