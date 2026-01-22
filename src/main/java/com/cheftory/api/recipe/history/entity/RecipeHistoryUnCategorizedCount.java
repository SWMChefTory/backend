package com.cheftory.api.recipe.history.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeHistoryUnCategorizedCount {
    private Integer count;

    public static RecipeHistoryUnCategorizedCount of(Integer count) {
        return new RecipeHistoryUnCategorizedCount(count);
    }
}
