package com.cheftory.api.ranking.candidate;

import com.cheftory.api.ranking.exception.RankingException;

public class RankingCandidateException extends RankingException {

    public RankingCandidateException(RankingCandidateErrorCode errorCode) {
        super(errorCode);
    }
}
