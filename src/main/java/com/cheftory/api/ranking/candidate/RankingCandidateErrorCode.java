package com.cheftory.api.ranking.candidate;

import com.cheftory.api.exception.Error;

/**
 * 랭킹 후보 검색 에러 코드
 */
public enum RankingCandidateErrorCode implements Error {
    RANKING_CANDIDATE_OPEN_FAILED("RANKING_CANDIDATE_001", "랭킹 후보 PIT 생성에 실패했습니다."),
    RANKING_CANDIDATE_SEARCH_FAILED("RANKING_CANDIDATE_002", "랭킹 후보 검색에 실패했습니다.");
    private final String errorCode;
    private final String message;

    RankingCandidateErrorCode(String errorCode, String message) {
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
