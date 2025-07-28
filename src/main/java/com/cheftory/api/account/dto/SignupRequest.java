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
    @JsonProperty("gender") @NotNull Gender gender,
    @JsonProperty("birth_of_date") @NotNull LocalDate birthOfDate
) {

}