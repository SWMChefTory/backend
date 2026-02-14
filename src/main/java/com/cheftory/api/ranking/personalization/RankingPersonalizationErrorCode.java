package com.cheftory.api.ranking.personalization;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;
import lombok.Getter;

@Getter
public enum RankingPersonalizationErrorCode implements Error {
    RANKING_PERSONALIZATION_SEARCH_FAILED("RANKING_PERSONALIZATION_001", "랭킹 개인화 검색에 실패했습니다.", ErrorType.INTERNAL);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    RankingPersonalizationErrorCode(String errorCode, String message, ErrorType type) {
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
