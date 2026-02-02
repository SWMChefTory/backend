package com.cheftory.api.recipe.bookmark.entity;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeBookmarkCategorizedCount {
    private UUID categoryId;
    private Integer count;

    public static RecipeBookmarkCategorizedCount of(UUID categoryId, Integer count) {
        return new RecipeBookmarkCategorizedCount(categoryId, count);
    }
}
