package com.cheftory.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * 토큰 재발급 요청 모델
 *
 * @param refreshToken 리프레시 토큰
 */
public record TokenReissueRequest(@JsonProperty("refresh_token") @NotNull String refreshToken) {}
