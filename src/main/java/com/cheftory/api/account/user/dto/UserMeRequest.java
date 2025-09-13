package com.cheftory.api.account.user.dto;

import com.cheftory.api.account.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;
import org.openapitools.jackson.nullable.JsonNullable;

public record UserMeRequest(
    Optional<String> nickname,
    JsonNullable<Gender> gender,
    @JsonProperty("date_of_birth") JsonNullable<LocalDate> dateOfBirth
) {

}
