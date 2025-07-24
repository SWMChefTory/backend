package com.cheftory.api.account.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record ExtractUserIdResponse(
    @JsonProperty("user_id")
    UUID userId
) {

  public static ExtractUserIdResponse of(UUID userId) {
    return new ExtractUserIdResponse(userId);
  }
}
