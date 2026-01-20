package com.cheftory.api.search.history;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SearchHistoriesResponse(
        @JsonProperty("recipe_search_histories") List<RecipeSearchHistoryResponse> histories) {

    public record RecipeSearchHistoryResponse(String history) {}

    public static SearchHistoriesResponse from(List<String> histories) {
        List<RecipeSearchHistoryResponse> historyResponses =
                histories.stream().map(RecipeSearchHistoryResponse::new).toList();

        return new SearchHistoriesResponse(historyResponses);
    }
}
