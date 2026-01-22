package com.cheftory.api.user.dto;

import com.cheftory.api.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        @JsonProperty("nickname") String nickname,
        @JsonProperty("gender") Gender gender,
        @JsonProperty("date_of_birth") LocalDate dateOfBirth,
        @JsonProperty("terms_of_use_agreed_at") LocalDateTime termsOfUseAgreedAt,
        @JsonProperty("privacy_agreed_at") LocalDateTime privacyAgreedAt,
        @JsonProperty("marketing_agreed_at") LocalDateTime marketingAgreedAt,
        @JsonProperty("provider_sub") String providerSub) {}
