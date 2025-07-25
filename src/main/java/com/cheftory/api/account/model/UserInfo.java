package com.cheftory.api.account.model;

import java.util.UUID;

public record UserInfo(
    UUID id,
    String email,
    String nickname
) {
  public static UserInfo from(UUID id, String email, String nickname) {
    return new UserInfo(id, email, nickname);
  }
}