package com.cheftory.api.ranking.personalization;

import com.cheftory.api.exception.Error;
import lombok.Getter;

@Getter
public enum RankingPersonalizationErrorCode implements Error {
    RANKING_PERSONALIZATION_SEARCH_FAILED("RANKING_PERSONALIZATION_001", "랭킹 개인화 검색에 실패했습니다.");

    private final String errorCode;
    private final String message;

    RankingPersonalizationErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
