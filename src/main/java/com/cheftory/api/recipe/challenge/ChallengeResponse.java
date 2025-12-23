package com.cheftory.api.recipe.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ChallengeResponse(
    @JsonProperty("challenge_id") String challengeId,
    @JsonProperty("start_at") LocalDateTime startAt,
    @JsonProperty("end_at") LocalDateTime endAt,
    @JsonProperty("type") String type) {
  public static ChallengeResponse of(Challenge challenge) {
    return new ChallengeResponse(
        challenge.getId().toString(),
        challenge.getStartAt(),
        challenge.getEndAt(),
        challenge.getType().name());
  }
}
