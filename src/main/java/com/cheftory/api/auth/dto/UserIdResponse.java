package com.cheftory.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * 유저 ID 응답 모델
 *
 * @param userId 유저 ID
 */
public record UserIdResponse(@JsonProperty("user_id") String userId) {
    public static UserIdResponse of(UUID userId) {
        return new UserIdResponse(userId.toString());
    }
}
