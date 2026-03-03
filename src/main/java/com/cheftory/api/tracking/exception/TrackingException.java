package com.cheftory.api.tracking.exception;

import com.cheftory.api.exception.CheftoryException;

/**
 * 레시피 추적 관련 예외
 */
public class TrackingException extends CheftoryException {

    public TrackingException(TrackingErrorCode errorCode) {
        super(errorCode);
    }

    public TrackingException(TrackingErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
