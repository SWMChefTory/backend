package com.cheftory.api.recipe.bookmark.entity;

import java.util.UUID;

/**
 * 카테고리별 레시피 북마크 개수 프로젝션
 */
public interface RecipeBookmarkCategorizedCountProjection {
    /**
     * 카테고리 ID 조회
     *
     * @return 카테고리 ID
     */
    UUID getCategoryId();

    /**
     * 북마크 개수 조회
     *
     * @return 북마크 개수
     */
    Long getCount();
}
