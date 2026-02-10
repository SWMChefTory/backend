package com.cheftory.api.recipe.creation.progress.entity;

/**
 * 레시피 생성 진행 단계 열거형
 *
 * <p>비동기 레시피 생성 파이프라인의 주요 단계를 나타냅니다.</p>
 */
public enum RecipeProgressStep {
    /**
     * 준비 단계 - 레시피 생성 시작 전
     */
    READY,
    /**
     * 캡션 단계 - YouTube 메타데이터 추출
     */
    CAPTION,
    /**
     * 브리핑 단계 - 레시피 개요 생성
     */
    BRIEFING,
    /**
     * 상세 단계 - 레시피 상세 정보 생성
     */
    DETAIL,
    /**
     * 조리단계 단계 - 레시피 조리 순서 생성
     */
    STEP,
    /**
     * 완료 단계 - 모든 생성 작업 완료
     */
    FINISHED
}
