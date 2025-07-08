package com.cheftory.api.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class YoutubeUrl {
    @Column(length=512, nullable = false)
    private URI uri;

    public static YoutubeUrl from(UriComponents uriComponents) {
        YoutubeUrlNormalizer normalizer = new YoutubeUrlNormalizer();
        UriComponents youtubeUriComponents = normalizer.normalize(uriComponents);
        return YoutubeUrl
                .builder()
                .uri(youtubeUriComponents.toUri())
                .build();
    }

    public String getVideoId(){
        UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
        return uriComponents
                .getQueryParams()
                .getFirst("v");
    }

    public String getUrl(){
        return uri.toString();
    }
}
