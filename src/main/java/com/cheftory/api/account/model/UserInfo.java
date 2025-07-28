package com.cheftory.api.account.model;

import com.cheftory.api.account.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;

public record UserInfo(
    UUID id,
    String email,
    String nickname,
    Gender gender,
    @JsonProperty("date_of_birth") LocalDate dateOfBirth
) {
  public static UserInfo from(
      UUID id,
      String email,
      String nickname,
      Gender gender,
      LocalDate dateOfBirth
  ) {
    return new UserInfo(id, email, nickname, gender, dateOfBirth);
  }
}