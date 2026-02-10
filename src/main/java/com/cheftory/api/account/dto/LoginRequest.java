package com.cheftory.api.account.dto;

import com.cheftory.api.user.entity.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * 로그인 요청 DTO.
 *
 * @param idToken OAuth ID 토큰
 * @param provider OAuth 제공자 (Apple, Kakao, etc.)
 */
public record LoginRequest(
        @JsonProperty("id_token") @NotNull String idToken, @JsonProperty("provider") @NotNull Provider provider) {}
