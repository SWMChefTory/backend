package com.cheftory.api.recipe.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class RecipePageRequest {
  private static final int DEFAULT_PAGE_SIZE = 10;

  public static Pageable create(int page, Sort sort) {
    return PageRequest.of(page, DEFAULT_PAGE_SIZE, sort);
  }
}