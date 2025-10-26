package com.cheftory.api.recipeinfo.search.autocomplete;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class RecipeAutocompletePageRequest {
  private static final int DEFAULT_PAGE_SIZE = 10;

  public static Pageable create(Integer page) {
    return PageRequest.of(page, DEFAULT_PAGE_SIZE);
  }
}
