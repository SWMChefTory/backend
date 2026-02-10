package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.Getter;

/**
 * 레시피 요리 타입 열거형
 *
 * <p>레시피의 요리 분류를 나타냅니다.</p>
 */
@Getter
public enum RecipeCuisineType {
    /**
     * 한식
     */
    KOREAN,
    /**
     * 분식
     */
    SNACK,
    /**
     * 중식
     */
    CHINESE,
    /**
     * 일식
     */
    JAPANESE,
    /**
     * 양식
     */
    WESTERN,
    /**
     * 디저트
     */
    DESSERT,
    /**
     * 건강식
     */
    HEALTHY,
    /**
     * 이유식
     */
    BABY,
    /**
     * 간단식
     */
    SIMPLE;

    /**
     * 메시지 키 반환
     *
     * @return i18n 메시지 키
     */
    public String messageKey() {
        return "recipe.cuisine." + name().toLowerCase();
    }

    /**
     * 문자열로부터 RecipeCuisineType 변환
     *
     * @param type 요리 타입 문자열
     * @return 변환된 요리 타입
     * @throws RecipeException 유효하지 않은 요리 타입인 경우
     */
    public static RecipeCuisineType fromString(String type) throws RecipeException {
        try {
            return valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new RecipeException(RecipeErrorCode.INVALID_CUISINE_TYPE);
        }
    }
}
