package com.cheftory.api.recipe.creation.progress.entity;

/**
 * 레시피 생성 상세 단계 열거형
 *
 * <p>각 진행 단계 내의 세부 작업 단계를 나타냅니다.</p>
 */
public enum RecipeProgressDetail {
    /**
     * 준비 완료
     */
    READY,
    /**
     * 자막 추출 중
     */
    CAPTION,
    /**
     * 태그 생성 중
     */
    TAG,
    /**
     * 상세 메타데이터 생성 중
     */
    DETAIL_META,
    /**
     * 재료 정보 생성 중
     */
    INGREDIENT,
    /**
     * 브리핑 생성 중
     */
    BRIEFING,
    /**
     * 조리 단계 생성 중
     */
    STEP,
    /**
     * 모든 작업 완료
     */
    FINISHED
}
