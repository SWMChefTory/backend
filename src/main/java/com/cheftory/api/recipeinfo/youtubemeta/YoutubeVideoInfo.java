package com.cheftory.api.recipeinfo.youtubemeta;

import jakarta.persistence.Embeddable;
import java.net.URI;
import lombok.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Embeddable
public class YoutubeVideoInfo {
  private URI videoUri;
  private String title;
  private URI thumbnailUrl;
  private Integer videoSeconds;

  public static YoutubeVideoInfo from(
      UriComponents uriComponents, String title, URI thumbnailUrl, Integer videoSeconds) {
    return YoutubeVideoInfo.builder()
        .videoUri(uriComponents.toUri())
        .title(title)
        .thumbnailUrl(thumbnailUrl)
        .videoSeconds(videoSeconds)
        .build();
  }

  public String getVideoId() {
    return UriComponentsBuilder.fromUri(videoUri).build().getQueryParams().getFirst("v");
  }
}
