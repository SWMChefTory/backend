package com.cheftory.api.recipe.challenge;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 챌린지 응답 DTO.
 *
 * @param challengeId 챌린지 ID
 * @param startAt 시작 시간
 * @param endAt 종료 시간
 * @param type 챌린지 타입
 */
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
