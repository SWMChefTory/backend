package com.cheftory.api.search.exception;

import com.cheftory.api.exception.Error;
import lombok.Getter;

/**
 * 검색 에러 코드.
 *
 * <p>검색 관련 에러 코드를 정의합니다.</p>
 */
@Getter
public enum SearchErrorCode implements Error {

    /** 레시피 검색 실패. */
    SEARCH_FAILED("SEARCH_001", "레시피 검색에 실패 했습니다."),

    /** 레시피 자동완성 실패. */
    AUTOCOMPLETE_FAILED("SEARCH_002", "레시피 자동완성에 실패 했습니다.");

    /** 에러 코드. */
    final String errorCode;

    /** 에러 메시지. */
    final String message;

    /**
     * 생성자.
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    SearchErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * 에러 코드를 반환합니다.
     *
     * @return 에러 코드
     */
    @Override
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 에러 메시지를 반환합니다.
     *
     * @return 에러 메시지
     */
    @Override
    public String getMessage() {
        return message;
    }
}
