package com.cheftory.api.recipe.bookmark.entity;

import java.util.UUID;

public interface RecipeBookmarkCategorizedCountProjection {
    UUID getCategoryId();

    Long getCount();
}
