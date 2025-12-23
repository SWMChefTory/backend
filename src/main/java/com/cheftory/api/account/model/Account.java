package com.cheftory.api.account.model;

import com.cheftory.api.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

  private String accessToken;
  private String refreshToken;
  private User user;

  public static Account of(String accessToken, String refreshToken, User user) {
    return new Account(accessToken, refreshToken, user);
  }
}
