package com.cheftory.api.user.dto;

import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 유저 관련 응답 DTO
 *
 * @param nickname 유저 닉네임
 * @param gender 성별
 * @param dateOfBirth 생년월일
 * @param termsOfUseAgreedAt 이용약관 동의일시
 * @param privacyAgreedAt 개인정보 처리방침 동의일시
 * @param marketingAgreedAt 마케팅 정보 수신 동의일시
 * @param tutorialAt 튜토리얼 완료일시
 * @param providerSub 소셜 로그인 제공자별 유저 고유 식별자
 */
public record UserResponse(@JsonProperty("nickname") String nickname, @JsonProperty("gender") Gender gender, @JsonProperty("date_of_birth") LocalDate dateOfBirth,
													 @JsonProperty("terms_of_use_agreed_at") LocalDateTime termsOfUseAgreedAt, @JsonProperty("privacy_agreed_at") LocalDateTime privacyAgreedAt,
													 @JsonProperty("marketing_agreed_at") LocalDateTime marketingAgreedAt, @JsonProperty("tutorial_at") LocalDateTime tutorialAt,
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
                user.getTutorialAt(),
                user.getProviderSub());
    }
}
