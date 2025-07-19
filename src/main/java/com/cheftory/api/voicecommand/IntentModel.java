package com.cheftory.api.voicecommand;

import lombok.Getter;

public enum IntentModel {
  GPT4_1,
  REGEX;

  public static IntentModel fromValue(String value) {
    return switch (value) {
      case "GPT4.1" -> GPT4_1;
      case "REGEX" -> REGEX;
      default -> throw new VoiceCommandHistoryException(
          VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL
      );
    };
  }
}