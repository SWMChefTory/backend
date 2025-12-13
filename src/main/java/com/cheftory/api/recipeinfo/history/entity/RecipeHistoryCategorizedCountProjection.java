package com.cheftory.api.recipeinfo.history.entity;

import java.util.UUID;

public interface RecipeHistoryCategorizedCountProjection {
  UUID getCategoryId();

  Long getCount();
}
