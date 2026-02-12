package com.cheftory.api.ranking.personalization;

import com.cheftory.api.ranking.exception.RankingException;

public class RankingPersonalizationException extends RankingException {
    public RankingPersonalizationException(RankingPersonalizationErrorCode errorCode) {
        super(errorCode);
    }
}
