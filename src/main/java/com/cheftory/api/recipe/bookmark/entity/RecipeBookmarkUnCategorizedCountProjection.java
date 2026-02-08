package com.cheftory.api.recipe.bookmark.entity;

/**
 * 카테고리 없는 레시피 북마크 개수 프로젝션
 */
public interface RecipeBookmarkUnCategorizedCountProjection {
    /**
     * 북마크 개수 조회
     *
     * @return 북마크 개수
     */
    Long getCount();
}
