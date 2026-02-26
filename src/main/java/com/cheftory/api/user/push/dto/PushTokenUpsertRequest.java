package com.cheftory.api.user.push.dto;

import com.cheftory.api.user.push.entity.PushTokenPlatform;
import com.cheftory.api.user.push.entity.PushTokenProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 푸시 토큰 등록/갱신 요청 DTO.
 *
 * @param provider 푸시 제공자
 * @param token 푸시 토큰 문자열 (최대 255자)
 * @param platform 디바이스 플랫폼
 */
public record PushTokenUpsertRequest(
        @JsonProperty("provider") @NotNull PushTokenProvider provider,
        @JsonProperty("token") @NotBlank @Size(max = 255) String token,
        @JsonProperty("platform") @NotNull PushTokenPlatform platform) {}
