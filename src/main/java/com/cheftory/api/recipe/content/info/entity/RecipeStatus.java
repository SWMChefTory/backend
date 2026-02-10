package com.cheftory.api.recipe.content.info.entity;

/**
 * 레시피 상태 열거형
 */
public enum RecipeStatus {
    /**
     * 생성 진행 중
     */
    IN_PROGRESS,
    /**
     * 생성 실패
     */
    FAILED,
    /**
     * 생성 성공 (활성 상태)
     */
    SUCCESS,
    /**
     * 차단됨
     */
    BLOCKED
}
