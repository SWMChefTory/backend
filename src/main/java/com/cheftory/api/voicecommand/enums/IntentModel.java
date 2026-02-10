package com.cheftory.api.voicecommand.enums;

import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import java.util.Arrays;
import lombok.Getter;

/**
 * 의도 파악(Intent) 모델 종류.
 */
@Getter
public enum IntentModel {
    GPT4_1("GPT4.1"),
    REGEX("REGEX"),
    NLU("NLU");

    private final String value;

    IntentModel(String value) {
        this.value = value;
    }

    /**
     * 문자열 값으로부터 의도 파악 모델을 찾아 반환합니다.
     *
     * @param value 의도 파악 모델 문자열 값
     * @return 의도 파악 모델 enum
     * @throws VoiceCommandHistoryException 지원하지 않는 의도 파악 모델인 경우
     */
    public static IntentModel fromValue(String value) throws VoiceCommandHistoryException {
        return Arrays.stream(values())
                .filter(model -> model.value.equals(value))
                .findFirst()
                .orElseThrow(() ->
                        new VoiceCommandHistoryException(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL));
    }
}
