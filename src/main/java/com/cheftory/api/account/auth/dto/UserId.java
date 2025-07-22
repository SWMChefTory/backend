package com.cheftory.api.account.auth.dto;

import lombok.Getter;

@Getter
public class UserId {
  private final String userId;

  public UserId(String userId) {
    this.userId = userId;
  }
}
