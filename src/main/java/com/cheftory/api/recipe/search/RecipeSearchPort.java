package com.cheftory.api.recipe.search;

import com.cheftory.api._common.cursor.CursorPage;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface RecipeSearchPort {
  @Deprecated(forRemoval = true)
  Page<UUID> searchRecipeIds(UUID userId, String query, int page);

  CursorPage<UUID> searchRecipeIds(UUID userId, String query, String cursor);
}
