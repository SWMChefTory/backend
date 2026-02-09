package com.cheftory.api.recipe.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.search.exception.SearchException;
import java.util.UUID;

public interface RecipeSearchPort {
    CursorPage<UUID> searchRecipeIds(UUID userId, String query, String cursor) throws SearchException;
}
