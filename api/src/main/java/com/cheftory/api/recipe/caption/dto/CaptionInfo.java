package com.cheftory.api.recipe.caption.dto;

import com.cheftory.api.recipe.caption.entity.Segment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CaptionInfo {
    List<Segment> segments;
    public static CaptionInfo from(List<Segment> segments) {
        return CaptionInfo
                .builder()
                .segments(segments)
                .build();
    }
}
