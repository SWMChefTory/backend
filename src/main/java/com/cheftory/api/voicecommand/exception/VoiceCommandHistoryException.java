package com.cheftory.api.voicecommand.exception;

import com.cheftory.api.exception.CheftoryException;

/**
 * 보이스 커맨드 히스토리 관련 예외.
 */
public class VoiceCommandHistoryException extends CheftoryException {
    public VoiceCommandHistoryException(VoiceCommandErrorCode errorCode) {
        super(errorCode);
    }

    public VoiceCommandHistoryException(VoiceCommandErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
