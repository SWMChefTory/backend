package com.cheftory.api.recipe.dto;

import org.springframework.data.domain.Sort;

/**
 * 레시피 정렬 관련 유틸리티 클래스
 */
public final class RecipeSort {
    /**
     * 조회수 내림차순 정렬 조건
     */
    public static final Sort COUNT_DESC = Sort.by(Sort.Direction.DESC, "viewCount");

    private RecipeSort() {
        throw new UnsupportedOperationException("Utility class");
    }
}
