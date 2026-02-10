package com.cheftory.api.voicecommand.enums;

import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import java.util.Arrays;
import lombok.Getter;

/**
 * STT(Speech-to-Text) 모델 종류.
 */
@Getter
public enum STTModel {
    VITO("VITO"),
    OPENAI("OPENAI"),
    CLOVA("CLOVA");

    private final String value;

    STTModel(String value) {
        this.value = value;
    }

    /**
     * 문자열 값으로부터 STT 모델을 찾아 반환합니다.
     *
     * @param value STT 모델 문자열 값
     * @return STT 모델 enum
     * @throws VoiceCommandHistoryException 지원하지 않는 STT 모델인 경우
     */
    public static STTModel fromValue(String value) throws VoiceCommandHistoryException {
        return Arrays.stream(values())
                .filter(model -> model.value.equals(value))
                .findFirst()
                .orElseThrow(
                        () -> new VoiceCommandHistoryException(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL));
    }
}
