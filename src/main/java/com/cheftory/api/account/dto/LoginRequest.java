package com.cheftory.api.account.dto;

import com.cheftory.api.account.user.entity.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
    @JsonProperty("id_token") @NotNull String idToken,
    @JsonProperty("provider") @NotNull Provider provider) {}
