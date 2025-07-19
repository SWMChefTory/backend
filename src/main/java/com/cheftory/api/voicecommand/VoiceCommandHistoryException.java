package com.cheftory.api.voicecommand;

import com.cheftory.api.user.exception.UserErrorCode;


public class VoiceCommandHistoryException extends RuntimeException {
  public final VoiceCommandErrorCode errorCode;

  public VoiceCommandHistoryException(VoiceCommandErrorCode errorCode) {
    this.errorCode = errorCode;
  }
}