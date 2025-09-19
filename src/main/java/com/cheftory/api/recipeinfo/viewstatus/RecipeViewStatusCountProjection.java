package com.cheftory.api.recipeinfo.viewstatus;

import java.util.UUID;

public interface RecipeViewStatusCountProjection {
  UUID getCategoryId();

  Long getCount();
}
