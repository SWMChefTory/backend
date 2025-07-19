package com.cheftory.api.voicecommand;

public enum STTModel {
  VITO;

  public static STTModel fromValue(String value) {
    return switch (value) {
      case "VITO" -> VITO;
      default -> throw new VoiceCommandHistoryException(
          VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL
      );    };
  }
}