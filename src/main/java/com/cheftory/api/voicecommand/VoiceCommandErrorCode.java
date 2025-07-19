package com.cheftory.api.voicecommand;

import com.cheftory.api.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum VoiceCommandErrorCode implements ErrorCode {

  VOICE_COMMAND_UNKNOWN_INTENT_MODEL("VOICE_COMMAND_1", "지원하지 않는 음성 명령 모델입니다."),
  VOICE_COMMAND_UNKNOWN_STT_MODEL("VOICE_COMMAND_2", "지원하지 않는 STT 모델입니다.");

  private final String code;
  private final String message;

  VoiceCommandErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }
}