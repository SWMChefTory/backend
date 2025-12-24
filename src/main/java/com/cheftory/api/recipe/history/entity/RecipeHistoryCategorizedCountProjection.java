package com.cheftory.api.recipe.history.entity;

import java.util.UUID;

public interface RecipeHistoryCategorizedCountProjection {
  UUID getCategoryId();

  Long getCount();
}
