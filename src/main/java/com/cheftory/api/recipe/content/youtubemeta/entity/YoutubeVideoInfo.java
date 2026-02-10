package com.cheftory.api.recipe.content.youtubemeta.entity;

import jakarta.persistence.Embeddable;
import java.net.URI;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유튜브 비디오 정보 Value Object
 */
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

    /**
     * 유튜브 비디오 정보 생성
     *
     * @param youtubeUri 유튜브 URI 객체
     * @param title 제목
     * @param channelTitle 채널명
     * @param thumbnailUrl 썸네일 URL
     * @param videoSeconds 비디오 길이 (초)
     * @param videoType 비디오 타입
     * @return 유튜브 비디오 정보 객체
     */
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
