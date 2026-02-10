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

/**
 * 유튜브 메타 정보 외부 클라이언트 구현체
 */
@Slf4j
@Component
public class YoutubeMetaExternalClient implements YoutubeMetaClient {

    private final WebClient webClient;

    @Value("${youtube.api-token-default}")
    private String youtubeDefaultKey;

    @Value("${youtube.api-token-block-check}")
    private String youtubeBlockCheckKey;

    public YoutubeMetaExternalClient(@Qualifier("youtubeClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 유튜브 비디오 정보 조회
     *
     * <p>YouTube Data API를 사용하여 비디오 정보를 조회하고, 쇼츠 여부 등을 판단합니다.</p>
     *
     * @param youtubeUri 유튜브 URI
     * @return 비디오 정보
     * @throws YoutubeMetaException API 호출 실패 또는 비디오 정보 조회 실패 시
     */
    @Override
    public YoutubeVideoInfo fetch(YoutubeUri youtubeUri) throws YoutubeMetaException {
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

    /**
     * 유튜브 비디오 차단 여부 확인
     *
     * <p>YouTube Data API를 사용하여 비디오의 상태 및 임베드 가능 여부를 확인합니다.</p>
     *
     * @param youtubeUri 유튜브 URI
     * @return 차단 여부 (true: 차단됨, false: 차단되지 않음)
     */
    @Override
    public Boolean isBlocked(YoutubeUri youtubeUri) {
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
