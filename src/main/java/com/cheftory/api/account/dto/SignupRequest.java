package com.cheftory.api.account.dto;

import com.cheftory.api.account.user.entity.Gender;
import com.cheftory.api.account.user.entity.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SignupRequest(
    @JsonProperty("id_token") @NotNull String idToken,
    @JsonProperty("provider") @NotNull Provider provider,
    @JsonProperty("nickname") @NotNull String nickname,
    @JsonProperty("gender") Gender gender,
    @JsonProperty("date_of_birth") LocalDate dateOfBirth,
    @JsonProperty("is_privacy_agreed") @NotNull boolean isPrivacyAgreed,
    @JsonProperty("is_terms_of_use_agreed") @NotNull boolean isTermsOfUseAgreed,
    @JsonProperty("is_marketing_agreed") @NotNull boolean isMarketingAgreed) {}
