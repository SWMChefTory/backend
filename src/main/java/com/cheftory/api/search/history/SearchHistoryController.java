package com.cheftory.api.search.history;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SearchHistoryController {
  private final SearchHistoryService searchHistoryService;

  @GetMapping("/recipes/search/history")
  @Deprecated(forRemoval = true, since = "v1")
  public SearchHistoriesResponse getRecipeSearchHistories(@UserPrincipal UUID userId) {
    List<String> histories = searchHistoryService.get(userId);
    return SearchHistoriesResponse.from(histories);
  }

  @Deprecated(forRemoval = true, since = "v1")
  @DeleteMapping(value = "/recipes/search/history", params = "searchText")
  public SuccessOnlyResponse deleteRecipeSearchHistory(
      @UserPrincipal UUID userId, @RequestParam("searchText") String searchText) {
    searchHistoryService.delete(userId, searchText);
    return SuccessOnlyResponse.create();
  }

  @Deprecated(forRemoval = true, since = "v1")
  @DeleteMapping("/recipes/search/history/all")
  public SuccessOnlyResponse deleteAllRecipeSearchHistories(@UserPrincipal UUID userId) {
    searchHistoryService.deleteAll(userId);
    return SuccessOnlyResponse.create();
  }

  @GetMapping("/search/histories")
  public SearchHistoriesResponse getSearchHistories(
      @UserPrincipal UUID userId,
      @RequestParam(value = "scope", defaultValue = "RECIPE") SearchHistoryScope scope) {
    return SearchHistoriesResponse.from(searchHistoryService.get(userId, scope));
  }

  @DeleteMapping(value = "/search/histories", params = "text")
  public SuccessOnlyResponse deleteSearchHistory(
      @UserPrincipal UUID userId,
      @RequestParam(value = "scope", defaultValue = "RECIPE") SearchHistoryScope scope,
      @RequestParam("text") String text) {
    searchHistoryService.delete(userId, scope, text);
    return SuccessOnlyResponse.create();
  }

  @DeleteMapping("/search/histories")
  public SuccessOnlyResponse deleteAllSearchHistories(
      @UserPrincipal UUID userId,
      @RequestParam(value = "scope", defaultValue = "RECIPE") SearchHistoryScope scope) {
    searchHistoryService.deleteAll(userId, scope);
    return SuccessOnlyResponse.create();
  }
}
