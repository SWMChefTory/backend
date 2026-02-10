package com.cheftory.api.recipe.creation.progress.entity;

/**
 * 레시피 생성 진행 상태 열거형
 *
 * <p>각 단계의 실행 상태를 나타냅니다.</p>
 */
public enum RecipeProgressState {
    /**
     * 실행 중
     */
    RUNNING,
    /**
     * 성공 완료
     */
    SUCCESS,
    /**
     * 실패
     */
    FAILED
}
