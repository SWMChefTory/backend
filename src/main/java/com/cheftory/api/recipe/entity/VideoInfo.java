package com.cheftory.api.recipe.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Embeddable
public class VideoInfo {
    private URI videoUri;
    private String title;
    private URI thumbnailUrl;

    private Integer videoSeconds;

    public static VideoInfo from(UriComponents uriComponents, String title, URI thumbnailUrl, Integer videoSeconds) {
        return VideoInfo.builder()
                .videoUri(uriComponents.toUri())
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .videoSeconds(videoSeconds).build();
    }

    public String getVideoId() {
        return UriComponentsBuilder
                .fromUri(videoUri)
                .build()
                .getQueryParams()
                .getFirst("v");
    }
}
