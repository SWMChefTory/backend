package com.cheftory.api.recipe.content.step.entity;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * 레시피 단계 정렬 관련 유틸리티 클래스
 */
public final class RecipeStepSort {
    /**
     * 단계 순서(stepOrder) 오름차순 정렬 조건
     */
    public static final Sort STEP_ORDER_ASC = Sort.by(Direction.ASC, "stepOrder");

    private RecipeStepSort() {
        throw new UnsupportedOperationException("Utility class");
    }
}
