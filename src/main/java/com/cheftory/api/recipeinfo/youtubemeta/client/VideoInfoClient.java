package com.cheftory.api.recipeinfo.youtubemeta.client;

import com.cheftory.api.recipeinfo.youtubemeta.YoutubeMetaType;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeUri;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeVideoInfo;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Slf4j
@Component
public class VideoInfoClient {

  private final WebClient webClient;

  @Value("${youtube.api-token}")
  private String youtubeKey;

  public VideoInfoClient(@Qualifier("youtubeClient") WebClient webClient) {
    this.webClient = webClient;
  }

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
                        .queryParam("key", youtubeKey)
                        .queryParam("part", "snippet,contentDetails")
                        .build())
            .retrieve()
            .bodyToMono(YoutubeVideoResponse.class)
            .block();

    if (youtubeVideoResponse == null || youtubeVideoResponse.items().isEmpty()) {
      throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_FOUND);
    }

    String channelId = youtubeVideoResponse.getChannelId();
    Long duration = youtubeVideoResponse.getSecondsDuration();

    if (duration == null) {
      throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_DURATION_NOT_FOUND);
    }

    boolean isShorts =
        Optional.ofNullable(channelId)
            .filter(id -> id.startsWith("UC"))
            .map(id -> "UUSH" + id.substring(2))
            .map(playlistId -> checkIfVideoInShortsPlaylist(videoId, playlistId))
            .orElse(false);

    // Shorts 플레이리스트에 없어도 60초 이하면 Shorts로 판별 (폴백)
    // (일부 채널은 UUSH를 지원하지 않음)
    if (!isShorts && duration <= 60) {
      isShorts = true;
    }

    YoutubeMetaType metaType = isShorts ? YoutubeMetaType.SHORTS : YoutubeMetaType.NORMAL;

    return YoutubeVideoInfo.from(
        youtubeUri,
        youtubeVideoResponse.getTitle(),
        URI.create(youtubeVideoResponse.getThumbnailUri()),
        duration.intValue(),
        metaType);
  }

  private boolean checkIfVideoInShortsPlaylist(String videoId, String shortsPlaylistId) {
    try {
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
                          .queryParam("key", youtubeKey)
                          .build())
              .retrieve()
              .bodyToMono(YoutubePlaylistResponse.class)
              .block();

      Objects.requireNonNull(playlistResponse, "플레이리스트 응답이 null 입니다.");

      return playlistResponse.hasItems();
    } catch (WebClientException e) {
      log.warn(
          "쇼츠 재생목록 검사 중 예기치 않은 오류가 발생했습니다: videoId={}, playlistId={}",
          videoId,
          shortsPlaylistId,
          e);
      return false;
    }
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
                        .queryParam("key", youtubeKey)
                        .queryParam("part", "id")
                        .build())
            .retrieve()
            .bodyToMono(YoutubeVideoResponse.class)
            .block();

    Objects.requireNonNull(youtubeVideoResponse, "비디오 응답이 null 입니다.");
    return youtubeVideoResponse.items().isEmpty();
  }
}
