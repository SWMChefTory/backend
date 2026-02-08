package com.cheftory.api.recipe.bookmark.entity;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리별 레시피 북마크 개수
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeBookmarkCategorizedCount {
    private UUID categoryId;
    private Integer count;

    /**
     * 카테고리별 레시피 북마크 개수 생성
     *
     * @param categoryId 카테고리 ID
     * @param count 북마크 개수
     * @return 카테고리별 북마크 개수 객체
     */
    public static RecipeBookmarkCategorizedCount of(UUID categoryId, Integer count) {
        return new RecipeBookmarkCategorizedCount(categoryId, count);
    }
}
