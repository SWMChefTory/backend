package com.cheftory.api.user.dto;

import com.cheftory.api.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import reactor.util.annotation.Nullable;

/**
 * 유저 관련 요청 DTO
 */
public record UserRequest() {
    /**
     * 유저 정보 수정 요청
     */
    public record Update(
            /**
             * 수정할 닉네임
             */
            String nickname,
            /**
             * 수정할 성별 (null 가능)
             */
            @Nullable Gender gender,
            /**
             * 수정할 생년월일 (null 가능)
             */
            @JsonProperty("date_of_birth") @Nullable LocalDate dateOfBirth) {}
}
