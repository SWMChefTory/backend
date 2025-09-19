package com.cheftory.api.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record LogoutRequest(@JsonProperty("refresh_token") @NotNull String refreshToken) {}
