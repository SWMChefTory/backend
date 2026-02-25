package com.cheftory.api.recipe.content.youtubemeta.entity;

import jakarta.persistence.Embeddable;
import java.net.URI;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * YouTube 비디오 메타 정보 Value Object.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class YoutubeVideoInfo {
    private String videoId;
    private String title;
    private String channelTitle;
    private URI thumbnailUrl;
    private Integer videoSeconds;
    private YoutubeMetaType videoType;

    /**
     * 유튜브 비디오 정보 생성
     *
     * @param videoId 비디오 ID
     * @param title 제목
     * @param channelTitle 채널명
     * @param thumbnailUrl 썸네일 URL
     * @param videoSeconds 비디오 길이 (초)
     * @param videoType 비디오 타입
     * @return 유튜브 비디오 정보 객체
     */
    public static YoutubeVideoInfo from(
            String videoId,
            String title,
            String channelTitle,
            URI thumbnailUrl,
            Integer videoSeconds,
            YoutubeMetaType videoType) {

        return new YoutubeVideoInfo(videoId, title, channelTitle, thumbnailUrl, videoSeconds, videoType);
    }
}
