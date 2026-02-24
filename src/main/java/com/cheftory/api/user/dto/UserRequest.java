package com.cheftory.api.user.dto;

import com.cheftory.api.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import java.time.LocalDate;

/**
 * 유저 관련 요청 DTO
 */
public record UserRequest() {
    /**
     * 유저 정보 수정 요청
     *
     * @param nickname 수정할 닉네임
     * @param gender 수정할 성별 (null 가능)
     * @param dateOfBirth 수정할 생년월일 (null 가능)
     */
    public record Update(
            String nickname,
            @Nullable Gender gender,
            @JsonProperty("date_of_birth") @Nullable LocalDate dateOfBirth) {}
}
