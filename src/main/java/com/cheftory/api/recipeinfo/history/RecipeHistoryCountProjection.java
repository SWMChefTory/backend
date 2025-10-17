package com.cheftory.api.recipeinfo.history;

import java.util.UUID;

public interface RecipeHistoryCountProjection {
  UUID getCategoryId();

  Long getCount();
}
