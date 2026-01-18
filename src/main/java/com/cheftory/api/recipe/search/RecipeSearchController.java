package com.cheftory.api.recipe.search;

import com.cheftory.api._common.security.UserPrincipal;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class RecipeSearchController {

  private final RecipeSearchFacade recipeSearchFacade;

  @GetMapping("/api/v1/recipes/search")
  public SearchedRecipesResponse searchRecipes(
      @RequestParam("query") String query,
      @RequestParam(required = false) @Min(0) Integer page,
      @RequestParam(required = false) String cursor,
      @UserPrincipal UUID userId) {
    if (page != null) {
      return SearchedRecipesResponse.from(recipeSearchFacade.searchRecipes(page, query, userId));
    }

    return SearchedRecipesResponse.from(recipeSearchFacade.searchRecipes(query, userId, cursor));
  }
}
