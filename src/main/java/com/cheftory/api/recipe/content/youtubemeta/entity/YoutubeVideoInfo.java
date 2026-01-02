package com.cheftory.api.recipe.content.youtubemeta.entity;

import jakarta.persistence.Embeddable;
import java.net.URI;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class YoutubeVideoInfo {
  private URI videoUri;
  private String videoId;
  private String title;
  private String channelTitle;
  private URI thumbnailUrl;
  private Integer videoSeconds;
  private YoutubeMetaType videoType;

  public static YoutubeVideoInfo from(
      YoutubeUri youtubeUri,
      String title,
      String channelTitle,
      URI thumbnailUrl,
      Integer videoSeconds,
      YoutubeMetaType videoType) {

    return new YoutubeVideoInfo(
        youtubeUri.getNormalizedUrl(),
        youtubeUri.getVideoId(),
        title,
        channelTitle,
        thumbnailUrl,
        videoSeconds,
        videoType);
  }
}
