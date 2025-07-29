package com.cheftory.api.account.user.dto;

import com.cheftory.api.account.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record UserMeResponse(
    @JsonProperty("nickname") String nickname,
    @JsonProperty("gender") Gender gender,
    @JsonProperty("date_of_birth") LocalDate dateOfBirth
) {

}
