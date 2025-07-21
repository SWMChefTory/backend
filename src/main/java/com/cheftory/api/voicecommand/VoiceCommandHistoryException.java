package com.cheftory.api.voicecommand;

import com.cheftory.api.exception.CheftoryException;

public class VoiceCommandHistoryException extends CheftoryException {
  public VoiceCommandHistoryException(VoiceCommandErrorCode errorCode) {
    super(errorCode);
  }

}