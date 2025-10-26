package com.cheftory.api.recipeinfo.search;

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

  public Page<RecipeSearch> search(String text, Integer page) {
    Pageable pageable = RecipeSearchPageRequest.create(page);
    return recipeSearchRepository.searchByKeyword(text, pageable);
  }
}
