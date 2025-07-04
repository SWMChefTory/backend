package com.cheftory.api.recipe.caption.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
public class CaptionFindResponse {
    private String segments;
    public static CaptionFindResponse of(String segments) {
        return CaptionFindResponse.builder()
                .segments(segments)
                .build();
    }
}
