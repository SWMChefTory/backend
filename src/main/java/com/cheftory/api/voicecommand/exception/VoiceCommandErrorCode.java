package com.cheftory.api.voicecommand.exception;

import com.cheftory.api.exception.Error;

/**
 * 보이스 커맨드 히스토리 관련 에러 코드.
 */
public enum VoiceCommandErrorCode implements Error {
    VOICE_COMMAND_UNKNOWN_INTENT_MODEL("VOICE_COMMAND_1", "지원하지 않는 음성 명령 모델입니다."),
    VOICE_COMMAND_UNKNOWN_STT_MODEL("VOICE_COMMAND_2", "지원하지 않는 STT 모델입니다.");

    private final String errorCode;
    private final String message;

    VoiceCommandErrorCode(String errorCode, String message) {
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
