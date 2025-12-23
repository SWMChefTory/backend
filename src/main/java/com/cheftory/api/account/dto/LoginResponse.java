package com.cheftory.api.account.dto;

import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoginResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("user_info") UserResponse user) {

  private record UserResponse(
      @JsonProperty("nickname") String nickname,
      @JsonProperty("gender") Gender gender,
      @JsonProperty("date_of_birth") LocalDate dateOfBirth,
      @JsonProperty("terms_of_use_agreed_at") LocalDateTime termsOfUseAgreedAt,
      @JsonProperty("privacy_agreed_at") LocalDateTime privacyAgreedAt,
      @JsonProperty("marketing_agreed_at") LocalDateTime marketingAgreedAt) {

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

  public static LoginResponse from(Account account) {
    return new LoginResponse(
        BearerAuthorizationUtils.addPrefix(account.getAccessToken()),
        BearerAuthorizationUtils.addPrefix(account.getRefreshToken()),
        UserResponse.from(account.getUser()));
  }
}
