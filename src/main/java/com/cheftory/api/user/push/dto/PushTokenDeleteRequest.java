package com.cheftory.api.user.push.dto;

import com.cheftory.api.user.push.entity.PushTokenProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 푸시 토큰 삭제(비활성화) 요청 DTO.
 *
 * @param provider 푸시 제공자
 * @param token 비활성화할 푸시 토큰 문자열 (최대 255자)
 */
public record PushTokenDeleteRequest(
        @JsonProperty("provider") @NotNull PushTokenProvider provider,
        @JsonProperty("token") @NotBlank @Size(max = 255) String token) {}
