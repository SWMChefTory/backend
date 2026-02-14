package com.cheftory.api.voicecommand.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 보이스 커맨드 히스토리 관련 에러 코드.
 */
public enum VoiceCommandErrorCode implements Error {
    VOICE_COMMAND_UNKNOWN_INTENT_MODEL("VOICE_COMMAND_001", "지원하지 않는 음성 명령 모델입니다.", ErrorType.VALIDATION),
    VOICE_COMMAND_UNKNOWN_STT_MODEL("VOICE_COMMAND_002", "지원하지 않는 STT 모델입니다.", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    VoiceCommandErrorCode(String errorCode, String message, ErrorType type) {
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
