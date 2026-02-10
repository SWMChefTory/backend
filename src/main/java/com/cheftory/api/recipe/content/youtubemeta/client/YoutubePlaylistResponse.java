package com.cheftory.api.recipe.content.youtubemeta.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * YouTube Playlist API 응답 DTO
 *
 * <p>YouTube Data API의 PlaylistItems 엔드포인트 응답을 담습니다.</p>
 *
 * @param items 플레이리스트 아이템 목록
 */
public record YoutubePlaylistResponse(List<Item> items) {

    /**
     * 아이템 존재 여부 확인
     *
     * @return 아이템이 하나 이상 존재하면 true
     */
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    /**
     * 플레이리스트 아이템 레코드
     *
     * @param snippet 비디오 기본 정보
     */
    public record Item(Snippet snippet) {}

    /**
     * 비디오 기본 정보 레코드
     *
     * @param videoId 비디오 ID
     */
    public record Snippet(@JsonProperty("videoId") String videoId) {}
}
