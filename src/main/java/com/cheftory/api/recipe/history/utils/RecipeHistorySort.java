package com.cheftory.api.recipe.history.utils;

import org.springframework.data.domain.Sort;

public final class RecipeHistorySort {
  public static final Sort VIEWED_AT_DESC = Sort.by(Sort.Direction.DESC, "viewedAt");
}
