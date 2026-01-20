package com.cheftory.api.search.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum SearchErrorCode implements ErrorMessage {
    SEARCH_FAILED("SEARCH_001", "레시피 검색에 실패 했습니다."),
    AUTOCOMPLETE_FAILED("SEARCH_002", "레시피 자동완성에 실패 했습니다.");
    final String errorCode;
    final String message;

    SearchErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
