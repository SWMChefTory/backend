package com.cheftory.api.recipeinfo.search.history;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecipeSearchHistoriesResponse(
    @JsonProperty("recipe_search_histories") List<RecipeSearchHistoryResponse> histories) {

  public record RecipeSearchHistoryResponse(String history) {}

  public static RecipeSearchHistoriesResponse from(List<String> histories) {
    List<RecipeSearchHistoryResponse> historyResponses =
        histories.stream().map(RecipeSearchHistoryResponse::new).toList();

    return new RecipeSearchHistoriesResponse(historyResponses);
  }
}
