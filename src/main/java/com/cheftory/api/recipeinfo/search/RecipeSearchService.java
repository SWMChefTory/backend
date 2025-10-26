package com.cheftory.api.recipeinfo.search;

import com.cheftory.api.recipeinfo.search.history.RecipeSearchHistoryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class RecipeSearchService {
  private final RecipeSearchRepository recipeSearchRepository;
  private final RecipeSearchHistoryService recipeSearchHistoryService;

  public Page<RecipeSearch> search(UUID userId, String text, Integer page) {
    Pageable pageable = RecipeSearchPageRequest.create(page);
    recipeSearchHistoryService.create(userId, text);
    return recipeSearchRepository.searchByKeyword(text, pageable);
  }
}
