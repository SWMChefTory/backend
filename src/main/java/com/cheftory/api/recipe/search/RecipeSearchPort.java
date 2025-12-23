package com.cheftory.api.recipe.search;

import java.util.UUID;
import org.springframework.data.domain.Page;

public interface RecipeSearchPort {
  Page<UUID> searchRecipeIds(UUID userId, String query, int page);
}
