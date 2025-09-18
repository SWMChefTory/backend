package com.cheftory.api.voicecommand.enums;

import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum STTModel {
  VITO("VITO"),
  OPENAI("OPENAI"),
  CLOVA("CLOVA");

  private final String value;

  STTModel(String value) {
    this.value = value;
  }

  public static STTModel fromValue(String value) {
    return Arrays.stream(values())
        .filter(model -> model.value.equals(value))
        .findFirst()
        .orElseThrow(() -> new VoiceCommandHistoryException(
            VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL));
  }
}