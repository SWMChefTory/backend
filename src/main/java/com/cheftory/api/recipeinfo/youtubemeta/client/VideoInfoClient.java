package com.cheftory.api.recipeinfo.youtubemeta.client;

import com.cheftory.api.recipeinfo.youtubemeta.YoutubeMetaType;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeUri;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeVideoInfo;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
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

    String channelId = youtubeVideoResponse.getChannelId();

    boolean isShorts =
        Optional.ofNullable(channelId)
            .filter(id -> id.startsWith("UC"))
            .map(id -> "UUSH" + id.substring(2))
            .map(playlistId -> checkIfVideoInShortsPlaylist(videoId, playlistId))
            .orElse(false);

    YoutubeMetaType metaType = isShorts ? YoutubeMetaType.SHORTS : YoutubeMetaType.NORMAL;

    return YoutubeVideoInfo.from(
        youtubeUri,
        youtubeVideoResponse.getTitle(),
        URI.create(youtubeVideoResponse.getThumbnailUri()),
        youtubeVideoResponse.getSecondsDuration().intValue(),
        metaType);
  }

  private boolean checkIfVideoInShortsPlaylist(String videoId, String shortsPlaylistId) {
    YoutubePlaylistResponse playlistResponse =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/playlistItems")
                        .queryParam("part", "snippet")
                        .queryParam("playlistId", shortsPlaylistId)
                        .queryParam("videoId", videoId)
                        .queryParam("key", YOUTUBE_KEY)
                        .build())
            .retrieve()
            .bodyToMono(YoutubePlaylistResponse.class)
            .block();

    Objects.requireNonNull(playlistResponse, "플레이리스트 응답이 null 입니다.");

    return playlistResponse.hasItems();
  }

  public Boolean isBlockedVideo(YoutubeUri youtubeUri) {
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
                        .queryParam("part", "id")
                        .build())
            .retrieve()
            .bodyToMono(YoutubeVideoResponse.class)
            .block();

    Objects.requireNonNull(youtubeVideoResponse, "비디오 응답이 null 입니다.");
    return youtubeVideoResponse.items().isEmpty();
  }
}
