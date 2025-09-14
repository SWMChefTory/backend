package com.cheftory.api.account.user.dto;

import com.cheftory.api.account.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import reactor.util.annotation.Nullable;

public record UpdateUserResponse(
    String nickname,
    @Nullable Gender gender,
    @JsonProperty("date_of_birth") @Nullable LocalDate dateOfBirth
) {

}
