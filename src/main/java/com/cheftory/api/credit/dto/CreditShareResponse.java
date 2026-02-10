package com.cheftory.api.credit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 크레딧 공유 횟수 응답 DTO.
 *
 * @param currentCount 현재 공유한 횟수
 * @param maxCount 최대 공유 가능 횟수
 */
public record CreditShareResponse(
        @JsonProperty("current_count") int currentCount, @JsonProperty("max_count") int maxCount) {
    /**
     * 공유 횟수로부터 응답 DTO를 생성합니다.
     *
     * @param currentCount 현재 공유한 횟수
     * @param maxCount 최대 공유 가능 횟수
     * @return 크레딧 공유 횟수 응답 DTO
     */
    public static CreditShareResponse of(int currentCount, int maxCount) {
        return new CreditShareResponse(currentCount, maxCount);
    }
}
