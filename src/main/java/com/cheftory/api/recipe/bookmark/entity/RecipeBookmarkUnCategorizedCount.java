package com.cheftory.api.recipe.bookmark.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 없는 레시피 북마크 개수
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeBookmarkUnCategorizedCount {
    private Integer count;

    /**
     * 카테고리 없는 레시피 북마크 개수 생성
     *
     * @param count 북마크 개수
     * @return 카테고리 없는 북마크 개수 객체
     */
    public static RecipeBookmarkUnCategorizedCount of(Integer count) {
        return new RecipeBookmarkUnCategorizedCount(count);
    }
}
