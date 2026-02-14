package com.cheftory.api.recipe.content.info.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 레시피 기본 정보 도메인 에러 코드 열거형
 *
 * <p>레시피 기본 정보 관련 작업 중 발생할 수 있는 에러 코드들을 정의합니다.</p>
 */
public enum RecipeInfoErrorCode implements Error {
    /**
     * 레시피 정보를 찾을 수 없음
     */
    RECIPE_INFO_NOT_FOUND("RECIPE_INFO_001", "레시피가 존재하지 않습니다.", ErrorType.NOT_FOUND),
    /**
     * 차단된 레시피
     */
    RECIPE_BANNED("RECIPE_INFO_002", "접근할 수 없는 레시피 입니다.", ErrorType.VALIDATION),
    /**
     * 생성 실패한 레시피
     */
    RECIPE_FAILED("RECIPE_INFO_003", "실패한 레시피 입니다.", ErrorType.INTERNAL),
    /**
     * 요리 비디오가 아님
     */
    RECIPE_NOT_COOK_VIDEO("RECIPE_INFO_004", "요리 비디오 id가 아닙니다.", ErrorType.VALIDATION),
    /**
     * 유효하지 않은 쿼리
     */
    RECIPE_NOT_VALID_QUERY("RECIPE_INFO_005", "유효하지 않은 레시피 조회 쿼리입니다.", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    /**
     * RecipeInfoErrorCode 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    RecipeInfoErrorCode(String errorCode, String message, ErrorType type) {
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
