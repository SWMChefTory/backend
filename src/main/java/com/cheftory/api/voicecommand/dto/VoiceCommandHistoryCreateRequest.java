package com.cheftory.api.voicecommand.dto;

import com.cheftory.api.user.validator.ExistsUserId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VoiceCommandHistoryCreateRequest(

    @NotNull
    @JsonProperty("baseIntent")
    String transcribe,

    @NotNull
    @JsonProperty("intent")
    String result,

    @NotNull
    @ExistsUserId
    @JsonProperty("userId")
    UUID userId,

    @NotNull
    @JsonProperty("sttModel")
    String sttModel,

    @NotNull
    @JsonProperty("intentModel")
    String intentModel

) {}