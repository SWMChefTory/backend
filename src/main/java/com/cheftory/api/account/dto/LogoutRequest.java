package com.cheftory.api.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * 로그아웃 요청 DTO.
 *
 * @param refreshToken 리프레시 토큰
 */
public record LogoutRequest(@JsonProperty("refresh_token") @NotNull String refreshToken) {}
