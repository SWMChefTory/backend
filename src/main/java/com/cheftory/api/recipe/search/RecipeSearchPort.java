package com.cheftory.api.recipe.search;

import com.cheftory.api._common.cursor.CursorPage;
import java.util.UUID;

public interface RecipeSearchPort {
    CursorPage<UUID> searchRecipeIds(UUID userId, String query, String cursor);
}
