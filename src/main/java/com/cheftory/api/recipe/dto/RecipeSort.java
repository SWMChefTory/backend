package com.cheftory.api.recipe.dto;

import org.springframework.data.domain.Sort;

public final class RecipeSort {
  public static final Sort COUNT_DESC = Sort.by(Sort.Direction.DESC, "viewCount");
}
