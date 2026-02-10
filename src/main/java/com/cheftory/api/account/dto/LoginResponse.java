package com.cheftory.api.account.dto;

import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 로그인/회원가입 응답 DTO.
 *
 * <p>액세스 토큰, 리프레시 토큰, 사용자 정보를 포함합니다.</p>
 *
 * @param accessToken Bearer 액세스 토큰
 * @param refreshToken Bearer 리프레시 토큰
 * @param user 사용자 정보
 */
public record LoginResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("user_info") UserResponse user) {

    /**
     * 사용자 정보 응답 레코드.
     *
     * @param nickname 사용자 닉네임
     * @param gender 성별
     * @param dateOfBirth 생년월일
     * @param termsOfUseAgreedAt 이용약관 동의 일시
     * @param privacyAgreedAt 개인정보 처리방침 동의 일시
     * @param marketingAgreedAt 마케팅 정보 수신 동의 일시
     */
    private record UserResponse(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("gender") Gender gender,
            @JsonProperty("date_of_birth") LocalDate dateOfBirth,
            @JsonProperty("terms_of_use_agreed_at") LocalDateTime termsOfUseAgreedAt,
            @JsonProperty("privacy_agreed_at") LocalDateTime privacyAgreedAt,
            @JsonProperty("marketing_agreed_at") LocalDateTime marketingAgreedAt) {

        /**
         * User 엔티티로부터 UserResponse를 생성합니다.
         *
         * @param user 사용자 엔티티
         * @return 사용자 정보 응답
         */
        private static UserResponse from(User user) {
            return new UserResponse(
                    user.getNickname(),
                    user.getGender(),
                    user.getDateOfBirth(),
                    user.getTermsOfUseAgreedAt(),
                    user.getPrivacyAgreedAt(),
                    user.getMarketingAgreedAt());
        }
    }

    /**
     * Account 객체로부터 LoginResponse를 생성합니다.
     *
     * @param account 계정 정보
     * @return 로그인 응답
     * @throws AuthException Bearer 토큰 생성 실패 시
     */
    public static LoginResponse from(Account account) throws AuthException {
        return new LoginResponse(
                BearerAuthorizationUtils.addPrefix(account.getAccessToken()),
                BearerAuthorizationUtils.addPrefix(account.getRefreshToken()),
                UserResponse.from(account.getUser()));
    }
}
