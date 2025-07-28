package com.cheftory.api.voicecommand.dto;

import com.cheftory.api.account.user.validator.ExistsUserId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VoiceCommandHistoryCreateRequest(

    @NotNull
    @JsonProperty("transcribe")
    String transcribe,

    @NotNull
    @JsonProperty("intent")
    String result,

    @NotNull
    @ExistsUserId
    @JsonProperty("user_id")
    UUID userId,

    @NotNull
    @JsonProperty("stt_model")
    String sttModel,

    @NotNull
    @JsonProperty("intent_model")
    String intentModel

) {}