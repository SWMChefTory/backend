package com.cheftory.api.recipe.model;

import org.springframework.data.domain.Sort;

public class RecipeSort {
  public static final Sort COUNT_DESC = Sort.by(Sort.Direction.DESC, "count");
}

