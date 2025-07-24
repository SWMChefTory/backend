package com.cheftory.api.voicecommand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VoiceCommandHistoryCreateResponse(
    @JsonProperty("message")
    String message
) {}