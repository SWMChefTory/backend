package com.cheftory.api.recipeinfo.history;

import java.util.UUID;

public interface RecipeHistoryCategorizedCountProjection {
  UUID getCategoryId();

  Long getCount();
}
