package com.cheftory.api.search;

import com.cheftory.api.recipe.search.RecipeSearchPort;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeSearchAdapter implements RecipeSearchPort {
  private final SearchFacade searchFacade;

  @Override
  public Page<UUID> searchRecipeIds(UUID userId, String query, int page) {
    Page<SearchQuery> results = searchFacade.search(userId, query, page);
    return results.map(s -> UUID.fromString(s.getId()));
  }
}
