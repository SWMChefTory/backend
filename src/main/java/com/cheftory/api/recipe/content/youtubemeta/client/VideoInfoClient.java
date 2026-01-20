package com.cheftory.api.recipe.content.youtubemeta.client;

import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
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

    @Value("${youtube.api-token-default}")
    private String youtubeDefaultKey;

    @Value("${youtube.api-token-block-check}")
    private String youtubeBlockCheckKey;

    public VideoInfoClient(@Qualifier("youtubeClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public YoutubeVideoInfo fetchVideoInfo(YoutubeUri youtubeUri) {
        String videoId = youtubeUri.getVideoId();

        try {
            YoutubeVideoResponse youtubeVideoResponse = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("id", videoId)
                            .queryParam("key", youtubeDefaultKey)
                            .queryParam("part", "snippet,contentDetails,status")
                            .build())
                    .retrieve()
                    .bodyToMono(YoutubeVideoResponse.class)
                    .block();

            if (youtubeVideoResponse == null || youtubeVideoResponse.items().isEmpty()) {
                throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_FOUND);
            }

            Boolean embeddable = youtubeVideoResponse.getEmbeddable();

            if (embeddable != null && !embeddable) {
                throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_EMBEDDABLE);
            }

            String channelId = youtubeVideoResponse.getChannelId();
            Long duration = youtubeVideoResponse.getSecondsDuration();

            if (duration == null) {
                throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_DURATION_NOT_FOUND);
            }

            boolean isShorts = Optional.ofNullable(channelId)
                    .filter(id -> id.startsWith("UC"))
                    .map(id -> "UUSH" + id.substring(2))
                    .map(playlistId -> checkIfVideoInShortsPlaylist(videoId, playlistId))
                    .orElse(false);

            if (!isShorts && duration <= 60) {
                isShorts = true;
            }

            YoutubeMetaType metaType = isShorts ? YoutubeMetaType.SHORTS : YoutubeMetaType.NORMAL;

            return YoutubeVideoInfo.from(
                    youtubeUri,
                    youtubeVideoResponse.getTitle(),
                    youtubeVideoResponse.getChannelTitle(),
                    URI.create(youtubeVideoResponse.getThumbnailUri()),
                    duration.intValue(),
                    metaType);

        } catch (YoutubeMetaException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("YouTube API 호출 실패 - videoId: {}, error: {}", videoId, e.getMessage());
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_API_ERROR);
        } catch (Exception e) {
            log.error("비디오 정보 조회 중 예상치 못한 오류 - videoId: {}", videoId, e);
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_API_ERROR);
        }
    }

    private boolean checkIfVideoInShortsPlaylist(String videoId, String shortsPlaylistId) {
        try {
            YoutubePlaylistResponse playlistResponse = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/playlistItems")
                            .queryParam("part", "snippet")
                            .queryParam("playlistId", shortsPlaylistId)
                            .queryParam("videoId", videoId)
                            .queryParam("key", youtubeDefaultKey)
                            .build())
                    .retrieve()
                    .bodyToMono(YoutubePlaylistResponse.class)
                    .block();

            return playlistResponse != null && playlistResponse.hasItems();

        } catch (WebClientException e) {
            log.warn("쇼츠 재생목록 검사 중 API 호출 실패: videoId={}, playlistId={}", videoId, shortsPlaylistId, e);
            return false;
        } catch (Exception e) {
            log.error("쇼츠 재생목록 검사 중 예상치 못한 오류: videoId={}, playlistId={}", videoId, shortsPlaylistId, e);
            return false;
        }
    }

    public Boolean isBlockedVideo(YoutubeUri youtubeUri) {
        String videoId = youtubeUri.getVideoId();

        try {
            YoutubeVideoResponse youtubeVideoResponse = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("id", videoId)
                            .queryParam("key", youtubeBlockCheckKey)
                            .queryParam("part", "status")
                            .build())
                    .retrieve()
                    .bodyToMono(YoutubeVideoResponse.class)
                    .block();

            if (youtubeVideoResponse == null || youtubeVideoResponse.items().isEmpty()) {
                return true;
            }

            Boolean embeddable = youtubeVideoResponse.getEmbeddable();
            return embeddable == null || !embeddable;

        } catch (WebClientException e) {
            log.warn("YouTube API 호출 실패 - videoId: {}", videoId, e);
            return false;
        } catch (Exception e) {
            log.error("예상치 못한 오류 - videoId: {}", videoId, e);
            return false;
        }
    }
}
