package com.cheftory.api.account.dto;

import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(
    @JsonProperty("id_token") @NotNull String idToken,
    @JsonProperty("provider") @NotNull Provider provider,
    @JsonProperty("nickname") @NotNull String nickname,
    @JsonProperty("gender") @NotNull Gender gender
) {

}