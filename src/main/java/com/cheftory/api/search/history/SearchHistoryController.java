package com.cheftory.api.search.history;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/search/history")
public class SearchHistoryController {
  private final SearchHistoryService searchHistoryService;

  @GetMapping
  public SearchHistoriesResponse getRecipeSearchHistories(@UserPrincipal UUID userId) {
    List<String> histories = searchHistoryService.get(userId);
    return SearchHistoriesResponse.from(histories);
  }

  @DeleteMapping
  public SuccessOnlyResponse deleteRecipeSearchHistory(
      @UserPrincipal UUID userId, String searchText) {
    searchHistoryService.delete(userId, searchText);
    return SuccessOnlyResponse.create();
  }

  @DeleteMapping("/all")
  public SuccessOnlyResponse deleteAllRecipeSearchHistories(@UserPrincipal UUID userId) {
    searchHistoryService.deleteAll(userId);
    return SuccessOnlyResponse.create();
  }
}
