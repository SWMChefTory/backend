package com.cheftory.api.recipe.creation.progress.utils;

import org.springframework.data.domain.Sort;

/**
 * 레시피 진행 상태 정렬 관련 유틸리티 클래스
 */
public final class RecipeProgressSort {
    /**
     * 생성일시 오름차순 정렬 조건
     */
    public static final Sort CREATE_AT_ASC = Sort.by(Sort.Direction.ASC, "createdAt");

    private RecipeProgressSort() {
        throw new UnsupportedOperationException("Utility class");
    }
}
