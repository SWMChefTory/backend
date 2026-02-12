package com.cheftory.api.ranking.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.Error;

public class RankingException extends CheftoryException {

    public RankingException(Error error) {
        super(error);
    }
}
