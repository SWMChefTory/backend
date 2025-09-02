package com.cheftory.api.account.user.dto;

import com.cheftory.api.account.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserMeResponse(
    @JsonProperty("nickname") String nickname,
    @JsonProperty("gender") Gender gender,
    @JsonProperty("date_of_birth") LocalDate dateOfBirth,
    @JsonProperty("terms_of_use_agreed_at") LocalDateTime termsOfUseAgreedAt,
    @JsonProperty("privacy_agreed_at") LocalDateTime privacyAgreedAt,
    @JsonProperty("marketing_agreed_at") LocalDateTime marketingAgreedAt
) {

}
