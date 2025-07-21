package com.cheftory.api.voicecommand;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum IntentModel {
  GPT4_1("GPT4.1"),
  REGEX("REGEX");

  private final String value;

  IntentModel(String value) {
    this.value = value;
  }

  public static IntentModel fromValue(String value) {
    return Arrays.stream(values())
        .filter(model -> model.value.equals(value))
        .findFirst()
        .orElseThrow(() -> new VoiceCommandHistoryException(
            VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL));
  }
}