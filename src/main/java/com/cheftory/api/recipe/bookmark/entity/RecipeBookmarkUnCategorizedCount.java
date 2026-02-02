package com.cheftory.api.recipe.bookmark.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeBookmarkUnCategorizedCount {
    private Integer count;

    public static RecipeBookmarkUnCategorizedCount of(Integer count) {
        return new RecipeBookmarkUnCategorizedCount(count);
    }
}
