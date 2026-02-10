package com.cheftory.api.account.dto;

import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 회원가입 요청 DTO.
 *
 * @param idToken OAuth ID 토큰
 * @param provider OAuth 제공자 (Apple, Kakao, etc.)
 * @param nickname 사용자 닉네임
 * @param gender 성별 (선택)
 * @param dateOfBirth 생년월일 (선택)
 * @param isPrivacyAgreed 개인정보 처리방침 동의 여부
 * @param isTermsOfUseAgreed 이용약관 동의 여부
 * @param isMarketingAgreed 마케팅 정보 수신 동의 여부
 */
public record SignupRequest(
        @JsonProperty("id_token") @NotNull String idToken,
        @JsonProperty("provider") @NotNull Provider provider,
        @JsonProperty("nickname") @NotNull String nickname,
        @JsonProperty("gender") Gender gender,
        @JsonProperty("date_of_birth") LocalDate dateOfBirth,
        @JsonProperty("is_privacy_agreed") @NotNull boolean isPrivacyAgreed,
        @JsonProperty("is_terms_of_use_agreed") @NotNull boolean isTermsOfUseAgreed,
        @JsonProperty("is_marketing_agreed") @NotNull boolean isMarketingAgreed) {}
