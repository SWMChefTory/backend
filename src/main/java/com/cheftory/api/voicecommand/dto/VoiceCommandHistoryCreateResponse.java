package com.cheftory.api.voicecommand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class VoiceCommandHistoryCreateResponse {

  @JsonProperty("message")
  String message;

  public VoiceCommandHistoryCreateResponse(String message) {
    this.message = message;
  }
}
