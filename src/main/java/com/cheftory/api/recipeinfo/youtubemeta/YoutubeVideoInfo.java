package com.cheftory.api.recipeinfo.youtubemeta;

import jakarta.persistence.Embeddable;
import java.net.URI;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Embeddable
public class YoutubeVideoInfo {
  private URI videoUri;
  private String videoId;
  private String title;
  private URI thumbnailUrl;
  private Integer videoSeconds;

  public static YoutubeVideoInfo from(
      YoutubeUri youtubeUri, String title, URI thumbnailUrl, Integer videoSeconds) {
    return YoutubeVideoInfo.builder()
        .videoUri(youtubeUri.getNormalizedUrl())
        .title(title)
        .thumbnailUrl(thumbnailUrl)
        .videoId(youtubeUri.getVideoId())
        .videoSeconds(videoSeconds)
        .build();
  }
}
