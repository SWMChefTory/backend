package com.cheftory.api.recipe.content.youtubemeta.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("YoutubeVideoResponse")
class YoutubeVideoResponseTest {

    @Test
    @DisplayName("getTitle()은 첫 번째 item의 title을 반환한다")
    void getTitleReturnsFirstItemTitle() {
        YoutubeVideoResponse.Snippet snippet = new YoutubeVideoResponse.Snippet("김치찌개 만들기", "UCabcd", "채널명", null);
        YoutubeVideoResponse.Status status = new YoutubeVideoResponse.Status(true);
        YoutubeVideoResponse.Item item = new YoutubeVideoResponse.Item(snippet, null, status);
        YoutubeVideoResponse response = new YoutubeVideoResponse(List.of(item));

        String title = response.getTitle();

        assertThat(title).isEqualTo("김치찌개 만들기");
    }

    @Test
    @DisplayName("getChannelId()는 첫 번째 item의 channelId를 반환한다")
    void getChannelIdReturnsFirstItemChannelId() {
        YoutubeVideoResponse.Snippet snippet = new YoutubeVideoResponse.Snippet("테스트 영상", "UCtest123", "테스트 채널", null);
        YoutubeVideoResponse.Status status = new YoutubeVideoResponse.Status(true);
        YoutubeVideoResponse.Item item = new YoutubeVideoResponse.Item(snippet, null, status);
        YoutubeVideoResponse response = new YoutubeVideoResponse(List.of(item));

        String channelId = response.getChannelId();

        assertThat(channelId).isEqualTo("UCtest123");
    }

    @Test
    @DisplayName("getThumbnailUri()는 maxres 썸네일 URL을 반환한다")
    void getThumbnailUriReturnsMaxresUrl() {
        YoutubeVideoResponse.ThumbnailInfo maxres =
                new YoutubeVideoResponse.ThumbnailInfo("https://i.ytimg.com/vi/test/maxresdefault.jpg", 1280, 720);
        YoutubeVideoResponse.Thumbnails thumbnails = new YoutubeVideoResponse.Thumbnails(null, null, null, maxres);
        YoutubeVideoResponse.Snippet snippet = new YoutubeVideoResponse.Snippet("영상", "UCabc", "채널", thumbnails);
        YoutubeVideoResponse.Status status = new YoutubeVideoResponse.Status(true);
        YoutubeVideoResponse.Item item = new YoutubeVideoResponse.Item(snippet, null, status);
        YoutubeVideoResponse response = new YoutubeVideoResponse(List.of(item));

        String thumbnailUri = response.getThumbnailUri();

        assertThat(thumbnailUri).isEqualTo("https://i.ytimg.com/vi/test/maxresdefault.jpg");
    }

    @Test
    @DisplayName("getThumbnailUri()는 maxres가 없으면 high 썸네일 URL을 반환한다")
    void getThumbnailUriReturnsHighUrlWhenMaxresMissing() {
        YoutubeVideoResponse.ThumbnailInfo high =
                new YoutubeVideoResponse.ThumbnailInfo("https://i.ytimg.com/vi/test/hqdefault.jpg", 480, 360);
        YoutubeVideoResponse.Thumbnails thumbnails = new YoutubeVideoResponse.Thumbnails(null, null, high, null);
        YoutubeVideoResponse.Snippet snippet = new YoutubeVideoResponse.Snippet("영상", "UCabc", "채널", thumbnails);
        YoutubeVideoResponse.Status status = new YoutubeVideoResponse.Status(true);
        YoutubeVideoResponse.Item item = new YoutubeVideoResponse.Item(snippet, null, status);
        YoutubeVideoResponse response = new YoutubeVideoResponse(List.of(item));

        String thumbnailUri = response.getThumbnailUri();

        assertThat(thumbnailUri).isEqualTo("https://i.ytimg.com/vi/test/hqdefault.jpg");
    }

    @Test
    @DisplayName("getThumbnailUri()는 썸네일 정보가 없으면 예외를 던진다")
    void getThumbnailUriThrowsWhenThumbnailMissing() {
        YoutubeVideoResponse.Snippet snippet = new YoutubeVideoResponse.Snippet("영상", "UCabc", "채널", null);
        YoutubeVideoResponse.Status status = new YoutubeVideoResponse.Status(true);
        YoutubeVideoResponse.Item item = new YoutubeVideoResponse.Item(snippet, null, status);
        YoutubeVideoResponse response = new YoutubeVideoResponse(List.of(item));

        com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException exception =
                assertThrows(
                        com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException.class,
                        response::getThumbnailUri);

        assertThat(exception.getErrorMessage().getMessage()).isEqualTo("썸네일 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("getSecondsDuration()은 ISO 8601 duration을 초로 변환한다")
    void getSecondsDurationConvertsIso8601ToSeconds() {
        YoutubeVideoResponse.ContentDetails contentDetails = new YoutubeVideoResponse.ContentDetails("PT10M30S");
        YoutubeVideoResponse.Snippet snippet = new YoutubeVideoResponse.Snippet("영상", "UCabc", "채널", null);
        YoutubeVideoResponse.Status status = new YoutubeVideoResponse.Status(true);
        YoutubeVideoResponse.Item item = new YoutubeVideoResponse.Item(snippet, contentDetails, status);
        YoutubeVideoResponse response = new YoutubeVideoResponse(List.of(item));

        Long seconds = response.getSecondsDuration();

        assertThat(seconds).isEqualTo(630L);
    }
}
