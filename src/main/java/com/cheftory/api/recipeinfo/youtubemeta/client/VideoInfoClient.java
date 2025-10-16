package com.cheftory.api.recipeinfo.youtubemeta.client;

import com.cheftory.api.recipeinfo.youtubemeta.YoutubeUri;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeVideoInfo;
import java.net.URI;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class VideoInfoClient {
  public VideoInfoClient(@Qualifier("youtubeClient") WebClient webClient) {
    this.webClient = webClient;
  }

  private final WebClient webClient;

  @Value("${youtube.api-token}")
  private String YOUTUBE_KEY;

  public YoutubeVideoInfo fetchVideoInfo(YoutubeUri youtubeUri) {
    String videoId = youtubeUri.getVideoId();

    YoutubeVideoResponse youtubeVideoResponse =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/videos")
                        .queryParam("id", videoId)
                        .queryParam("key", YOUTUBE_KEY)
                        .queryParam("part", "snippet,contentDetails")
                        .build())
            .retrieve()
            .bodyToMono(YoutubeVideoResponse.class)
            .block();

    Objects.requireNonNull(youtubeVideoResponse, "비디오 응답이 null 입니다.");

    return YoutubeVideoInfo.from(
        youtubeUri,
        youtubeVideoResponse.getTitle(),
        URI.create(youtubeVideoResponse.getThumbnailUri()),
        youtubeVideoResponse.getSecondsDuration().intValue());
  }
}
