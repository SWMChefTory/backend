package com.cheftory.api.recipe.dto;

/**
 * 레시피 비디오 조회 타입 열거형
 *
 * <p>레시피 목록 조회 시 포함할 비디오 타입을 지정합니다.</p>
 */
public enum RecipeInfoVideoQuery {
    /**
     * 모든 비디오 타입 포함
     */
    ALL,
    /**
     * 일반 비디오만 포함
     */
    NORMAL,
    /**
     * Shorts 비디오만 포함
     */
    SHORTS
}
