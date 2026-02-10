package com.cheftory.api.search.history;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 검색 히스토리 응답 DTO.
 *
 * @param histories 검색 히스토리 목록
 */
public record SearchHistoriesResponse(
        @JsonProperty("recipe_search_histories") List<RecipeSearchHistoryResponse> histories) {

    /**
     * 레시피 검색 히스토리 응답 DTO.
     *
     * @param history 검색어
     */
    public record RecipeSearchHistoryResponse(String history) {}

    /**
     * 검색어 목록으로부터 응답을 생성합니다.
     *
     * @param histories 검색어 목록
     * @return 검색 히스토리 응답
     */
    public static SearchHistoriesResponse from(List<String> histories) {
        List<RecipeSearchHistoryResponse> historyResponses =
                histories.stream().map(RecipeSearchHistoryResponse::new).toList();

        return new SearchHistoriesResponse(historyResponses);
    }
}
