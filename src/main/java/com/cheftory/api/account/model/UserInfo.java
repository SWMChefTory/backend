package com.cheftory.api.account.model;

import com.cheftory.api.account.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record UserInfo(
    String nickname,
    Gender gender,
    @JsonProperty("date_of_birth") LocalDate dateOfBirth
) {
  public static UserInfo from(
      String nickname,
      Gender gender,
      LocalDate dateOfBirth
  ) {
    return new UserInfo(nickname, gender, dateOfBirth);
  }
}