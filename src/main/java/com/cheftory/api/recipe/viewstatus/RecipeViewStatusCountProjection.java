package com.cheftory.api.recipe.viewstatus;

import java.util.UUID;

public interface RecipeViewStatusCountProjection {
  UUID getCategoryId();
  Long getCount();
}