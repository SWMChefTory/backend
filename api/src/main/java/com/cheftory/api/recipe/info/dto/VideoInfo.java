package com.cheftory.api.recipe.info.dto;

import com.cheftory.api.recipe.info.entity.RecipeStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.net.URI;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access= AccessLevel.PRIVATE)
public class VideoInfo {
    private final URI videoUrl;
    private final Integer videoSeconds;
    private final URI thumbnailUrl;

    public static VideoInfo of(URI videoUrl, Integer videoSeconds, URI thumbnailUrl) {
        return VideoInfo.builder()
                .videoUrl(videoUrl)
                .videoSeconds(videoSeconds)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
