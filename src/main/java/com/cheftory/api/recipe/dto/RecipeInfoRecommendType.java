package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 추천 타입 열거형
 *
 * <p>레시피 추천 시 사용할 추천 알고리즘 타입을 지정합니다.</p>
 */
public enum RecipeInfoRecommendType {
    /**
     * 인기 레시피
     */
    POPULAR,
    /**
     * 트렌딩 레시피
     */
    TRENDING,
    /**
     * 셰프 추천 레시피
     */
    CHEF;

    /**
     * 문자열로부터 RecipeInfoRecommendType 변환
     *
     * @param type 추천 타입 문자열
     * @return 변환된 추천 타입
     * @throws RecipeException 유효하지 않은 추천 타입인 경우
     */
    public static RecipeInfoRecommendType fromString(String type) throws RecipeException {
        try {
            return RecipeInfoRecommendType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new RecipeException(RecipeErrorCode.INVALID_RECOMMEND_TYPE);
        }
    }
}
