package com.cheftory.api.user.dto;

import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 유저 관련 응답 DTO
 */
public record UserResponse(
        /**
         * 유저 닉네임
         */
        @JsonProperty("nickname") String nickname,
        /**
         * 성별
         */
        @JsonProperty("gender") Gender gender,
        /**
         * 생년월일
         */
        @JsonProperty("date_of_birth") LocalDate dateOfBirth,
        /**
         * 이용약관 동의일시
         */
        @JsonProperty("terms_of_use_agreed_at") LocalDateTime termsOfUseAgreedAt,
        /**
         * 개인정보 처리방침 동의일시
         */
        @JsonProperty("privacy_agreed_at") LocalDateTime privacyAgreedAt,
        /**
         * 마케팅 정보 수신 동의일시
         */
        @JsonProperty("marketing_agreed_at") LocalDateTime marketingAgreedAt,
        /**
         * 소셜 로그인 제공자별 유저 고유 식별자
         */
        @JsonProperty("provider_sub") String providerSub) {
    /**
     * User 엔티티로부터 UserResponse 생성
     *
     * @param user 유저 엔티티
     * @return 유저 응답 DTO
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getNickname(),
                user.getGender(),
                user.getDateOfBirth(),
                user.getTermsOfUseAgreedAt(),
                user.getPrivacyAgreedAt(),
                user.getMarketingAgreedAt(),
                user.getProviderSub());
    }
}
