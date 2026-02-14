package com.cheftory.api.recipe.creation.identify.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;
import lombok.Getter;

/**
 * 레시피 식별 도메인 에러 코드 열거형
 *
 * <p>레시피 생성 식별 관련 작업 중 발생할 수 있는 에러 코드들을 정의합니다.</p>
 */
@Getter
public enum RecipeIdentifyErrorCode implements Error {
    /**
     * 레시피 생성이 이미 진행중인 경우
     */
    RECIPE_IDENTIFY_PROGRESSING("RECIPE_IDENTIFY_001", "레시피 생성이 진행중입니다.", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    /**
     * RecipeIdentifyErrorCode 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    RecipeIdentifyErrorCode(String errorCode, String message, ErrorType type) {
        this.errorCode = errorCode;
        this.message = message;
        this.type = type;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ErrorType getType() {
        return type;
    }
}
