package com.cheftory.api.account.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record TokenReissueRequest(@JsonProperty("refresh_token") @NotNull String refreshToken) {}
