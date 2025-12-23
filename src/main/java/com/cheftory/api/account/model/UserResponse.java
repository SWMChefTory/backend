package com.cheftory.api.account.model;

import com.cheftory.api.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
    String nickname,
    Gender gender,
    @JsonProperty("date_of_birth") LocalDate dateOfBirth,
    @JsonProperty("terms_of_use_agreed_at") LocalDateTime termsOfUseAgreedAt,
    @JsonProperty("privacy_agreed_at") LocalDateTime privacyAgreedAt,
    @JsonProperty("marketing_agreed_at") LocalDateTime marketingAgreedAt) {
  public static UserResponse from(
      String nickname,
      Gender gender,
      LocalDate dateOfBirth,
      LocalDateTime termsOfUseAgreedAt,
      LocalDateTime privacyAgreedAt,
      LocalDateTime marketingAgreedAt) {
    return new UserResponse(
        nickname, gender, dateOfBirth, termsOfUseAgreedAt, privacyAgreedAt, marketingAgreedAt);
  }
}
