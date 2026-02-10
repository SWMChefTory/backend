package com.cheftory.api.recipe.content.youtubemeta.client;

import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.util.Iso8601DurationToSecondConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * YouTube Video API 응답 DTO
 *
 * <p>YouTube Data API의 Videos 엔드포인트 응답을 담습니다.</p>
 * <p>비디오의 제목, 썸네일, 재생시간, 채널 정보 등을 추출할 수 있습니다.</p>
 *
 * @param items 비디오 정보 목록
 */
public record YoutubeVideoResponse(List<Item> items) {

    /**
     * 썸네일 URI 추출
     *
     * <p>가장 높은 해상도의 썸네일을 우선으로 반환합니다.</p>
     * <p>우선순위: maxres -> high -> medium -> default</p>
     *
     * @return 썸네일 URL
     * @throws YoutubeMetaException 썸네일을 찾을 수 없을 경우
     */
    public String getThumbnailUri() throws YoutubeMetaException {
        Thumbnails thumbnails = items.getFirst().snippet().thumbnails();
        if (thumbnails == null) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_THUMBNAIL_NOT_FOUND);
        }

        ThumbnailInfo thumbnail = firstAvailableThumbnail(thumbnails);
        if (thumbnail == null) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_THUMBNAIL_NOT_FOUND);
        }

        return thumbnail.url();
    }

    /**
     * 사용 가능한 첫 번째 썸네일 반환
     *
     * @param thumbnails 썸네일 객체
     * @return 사용 가능한 썸네일 정보
     */
    private ThumbnailInfo firstAvailableThumbnail(Thumbnails thumbnails) {
        if (thumbnails.maxres() != null) {
            return thumbnails.maxres();
        }
        if (thumbnails.high() != null) {
            return thumbnails.high();
        }
        if (thumbnails.medium() != null) {
            return thumbnails.medium();
        }
        return thumbnails.defaultThumbnail();
    }

    /**
     * 비디오 제목 추출
     *
     * @return 비디오 제목
     */
    public String getTitle() {
        return items.getFirst().snippet().title();
    }

    /**
     * 비디오 재생시간 추출 (초 단위)
     *
     * <p>ISO 8601 형식의 duration을 초로 변환합니다.</p>
     *
     * @return 재생시간 (초), duration이 null인 경우 null
     */
    public Long getSecondsDuration() {
        String duration = items.getFirst().contentDetails().duration();
        if (duration == null) {
            return null;
        }
        return Iso8601DurationToSecondConverter.convert(duration);
    }

    /**
     * 채널 ID 추출
     *
     * @return 채널 ID
     */
    public String getChannelId() {
        return items.getFirst().snippet().channelId();
    }

    /**
     * 채널 제목 추출
     *
     * @return 채널 제목
     */
    public String getChannelTitle() {
        return items.getFirst().snippet().channelTitle();
    }

    /**
     * 임베드 가능 여부 추출
     *
     * @return 임베드 가능 여부
     */
    public Boolean getEmbeddable() {
        return items.stream()
                .findFirst()
                .map(Item::status)
                .map(Status::embeddable)
                .orElse(null);
    }

    /**
     * 비디오 정보 아이템 레코드
     *
     * @param snippet 비디오 기본 정보
     * @param contentDetails 비디오 상세 정보
     * @param status 비디오 상태 정보
     */
    public record Item(Snippet snippet, ContentDetails contentDetails, Status status) {}

    /**
     * 비디오 기본 정보 레코드
     *
     * @param title 비디오 제목
     * @param channelId 채널 ID
     * @param channelTitle 채널 제목
     * @param thumbnails 썸네일 정보
     */
    public record Snippet(String title, String channelId, String channelTitle, Thumbnails thumbnails) {}

    /**
     * 썸네일 정보 레코드
     *
     * @param defaultThumbnail 기본 썸네일
     * @param medium 중간 해상도 썸네일
     * @param high 높은 해상도 썸네일
     * @param maxres 최대 해상도 썸네일
     */
    public record Thumbnails(
            @JsonProperty("default") ThumbnailInfo defaultThumbnail,
            ThumbnailInfo medium,
            ThumbnailInfo high,
            ThumbnailInfo maxres) {}

    /**
     * 썸네일 상세 정보 레코드
     *
     * @param url 썸네일 URL
     * @param width 가로 크기
     * @param height 세로 크기
     */
    public record ThumbnailInfo(String url, int width, int height) {}

    /**
     * 비디오 상세 정보 레코드
     *
     * @param duration ISO 8601 형식의 재생시간
     */
    public record ContentDetails(String duration) {}

    /**
     * 비디오 상태 정보 레코드
     *
     * @param embeddable 임베드 가능 여부
     */
    public record Status(Boolean embeddable) {}
}
