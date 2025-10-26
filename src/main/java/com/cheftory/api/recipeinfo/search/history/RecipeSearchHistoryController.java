package com.cheftory.api.recipeinfo.search.history;

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
public class RecipeSearchHistoryController {
  private final RecipeSearchHistoryService recipeSearchHistoryService;

  @GetMapping
  public RecipeSearchHistoriesResponse getRecipeSearchHistories(@UserPrincipal UUID userId) {
    List<String> histories = recipeSearchHistoryService.get(userId);
    return RecipeSearchHistoriesResponse.from(histories);
  }

  @DeleteMapping
  public SuccessOnlyResponse deleteRecipeSearchHistory(
      @UserPrincipal UUID userId, String searchText) {
    recipeSearchHistoryService.delete(userId, searchText);
    return SuccessOnlyResponse.create();
  }

  @DeleteMapping("/all")
  public SuccessOnlyResponse deleteAllRecipeSearchHistories(@UserPrincipal UUID userId) {
    recipeSearchHistoryService.deleteAll(userId);
    return SuccessOnlyResponse.create();
  }
}
