package com.cheftory.api.credit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreditShareResponse(
        @JsonProperty("current_count") int currentCount, @JsonProperty("max_count") int maxCount) {
    public static CreditShareResponse of(int currentCount, int maxCount) {
        return new CreditShareResponse(currentCount, maxCount);
    }
}
