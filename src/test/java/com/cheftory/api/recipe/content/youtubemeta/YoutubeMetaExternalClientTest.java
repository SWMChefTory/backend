package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaExternalClient;
import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaHttpApi;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.io.IOException;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@DisplayName("YoutubeMetaExternalClient 테스트")
class YoutubeMetaExternalClientTest {

    private MockWebServer mockWebServer;
    private YoutubeMetaExternalClient sut;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        YoutubeMetaHttpApi api = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build()
                .createClient(YoutubeMetaHttpApi.class);

        sut = new YoutubeMetaExternalClient(api);
        ReflectionTestUtils.setField(sut, "youtubeDefaultKey", "default-key");
        ReflectionTestUtils.setField(sut, "youtubeBlockCheckKey", "block-key");
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("fetch(videoId)")
    class Fetch {
        @Nested
        @DisplayName("Given - 정상 API 응답")
        class GivenSuccessResponse {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("""
                        {
                          "items": [{
                            "snippet": {
                              "title": "title",
                              "channelId": "UCabc123",
                              "channelTitle": "channel",
                              "thumbnails": { "maxres": { "url": "https://i.ytimg.com/vi/abc/maxresdefault.jpg", "width": 1280, "height": 720 } }
                            },
                            "contentDetails": { "duration": "PT2M30S" },
                            "status": { "embeddable": true }
                          }]
                        }
                        """));
                mockWebServer.enqueue(jsonResponse("{\"items\": []}"));
            }

            @Test
            @DisplayName("Then - 비디오 정보를 반환한다")
            void thenReturnsInfo() throws Exception {
                YoutubeVideoInfo result = sut.fetch("abc");

                assertThat(result.getVideoId()).isEqualTo("abc");
                assertThat(result.getTitle()).isEqualTo("title");
                assertThat(result.getVideoSeconds()).isEqualTo(150);
                assertThat(result.getVideoType()).isEqualTo(YoutubeMetaType.NORMAL);

                RecordedRequest first = mockWebServer.takeRequest();
                assertThat(first.getTarget()).contains("/videos").contains("id=abc");
            }
        }

        @Nested
        @DisplayName("Given - items가 비어 있는 응답")
        class GivenEmptyItems {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("{\"items\": []}"));
            }

            @Test
            @DisplayName("Then - VIDEO_NOT_FOUND 예외를 던진다")
            void thenThrowsNotFound() {
                assertThatThrownBy(() -> sut.fetch("missing"))
                        .isInstanceOf(YoutubeMetaException.class)
                        .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_FOUND);
            }
        }

        @Nested
        @DisplayName("Given - embeddable=false 응답")
        class GivenNotEmbeddable {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("""
                        {
                          "items": [{
                            "snippet": {
                              "title": "title",
                              "channelId": "UCabc123",
                              "channelTitle": "channel",
                              "thumbnails": { "maxres": { "url": "https://i.ytimg.com/vi/abc/maxresdefault.jpg", "width": 1280, "height": 720 } }
                            },
                            "contentDetails": { "duration": "PT2M30S" },
                            "status": { "embeddable": false }
                          }]
                        }
                        """));
            }

            @Test
            @DisplayName("Then - VIDEO_NOT_EMBEDDABLE 예외를 던진다")
            void thenThrowsNotEmbeddable() {
                assertThatThrownBy(() -> sut.fetch("abc"))
                        .isInstanceOf(YoutubeMetaException.class)
                        .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_EMBEDDABLE);
            }
        }

        @Nested
        @DisplayName("Given - duration 이 없는 응답")
        class GivenDurationMissing {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("""
                        {
                          "items": [{
                            "snippet": {
                              "title": "title",
                              "channelId": "UCabc123",
                              "channelTitle": "channel",
                              "thumbnails": { "maxres": { "url": "https://i.ytimg.com/vi/abc/maxresdefault.jpg", "width": 1280, "height": 720 } }
                            },
                            "contentDetails": {},
                            "status": { "embeddable": true }
                          }]
                        }
                        """));
            }

            @Test
            @DisplayName("Then - VIDEO_DURATION_NOT_FOUND 예외를 던진다")
            void thenThrowsDurationNotFound() {
                assertThatThrownBy(() -> sut.fetch("abc"))
                        .isInstanceOf(YoutubeMetaException.class)
                        .hasFieldOrPropertyWithValue(
                                "error", YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_DURATION_NOT_FOUND);
            }
        }

        @Nested
        @DisplayName("Given - thumbnail 이 없는 응답")
        class GivenThumbnailMissing {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("""
                        {
                          "items": [{
                            "snippet": {
                              "title": "title",
                              "channelId": "UCabc123",
                              "channelTitle": "channel",
                              "thumbnails": {}
                            },
                            "contentDetails": { "duration": "PT2M30S" },
                            "status": { "embeddable": true }
                          }]
                        }
                        """));
                mockWebServer.enqueue(jsonResponse("{\"items\": []}"));
            }

            @Test
            @DisplayName("Then - THUMBNAIL_NOT_FOUND 예외를 던진다")
            void thenThrowsThumbnailNotFound() {
                assertThatThrownBy(() -> sut.fetch("abc"))
                        .isInstanceOf(YoutubeMetaException.class)
                        .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_THUMBNAIL_NOT_FOUND);
            }
        }

        @Nested
        @DisplayName("Given - 쇼츠 재생목록에 포함된 응답")
        class GivenShortsPlaylistHit {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("""
                        {
                          "items": [{
                            "snippet": {
                              "title": "title",
                              "channelId": "UCabc123",
                              "channelTitle": "channel",
                              "thumbnails": { "maxres": { "url": "https://i.ytimg.com/vi/abc/maxresdefault.jpg", "width": 1280, "height": 720 } }
                            },
                            "contentDetails": { "duration": "PT5M" },
                            "status": { "embeddable": true }
                          }]
                        }
                        """));
                mockWebServer.enqueue(jsonResponse("""
                        { "items": [{ "snippet": { "title": "shorts item" } }] }
                        """));
            }

            @Test
            @DisplayName("Then - SHORTS 타입으로 반환한다")
            void thenReturnsShorts() throws Exception {
                YoutubeVideoInfo result = sut.fetch("abc");
                assertThat(result.getVideoType()).isEqualTo(YoutubeMetaType.SHORTS);
            }
        }

        @Nested
        @DisplayName("Given - API 오류 응답")
        class GivenApiError {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(
                        new MockResponse.Builder().code(500).body("boom").build());
            }

            @Test
            @DisplayName("Then - API_ERROR 예외를 던진다")
            void thenThrowsApiError() {
                assertThatThrownBy(() -> sut.fetch("abc"))
                        .isInstanceOf(YoutubeMetaException.class)
                        .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_API_ERROR);
            }
        }
    }

    @Nested
    @DisplayName("isBlocked(videoId)")
    class IsBlocked {
        @Nested
        @DisplayName("Given - embeddable=false 응답")
        class GivenNotEmbeddable {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("""
                        { "items": [{ "status": { "embeddable": false } }] }
                        """));
            }

            @Test
            @DisplayName("Then - true를 반환한다")
            void thenReturnsTrue() {
                assertThat(sut.isBlocked("abc")).isTrue();
            }
        }

        @Nested
        @DisplayName("Given - items가 비어 있는 응답")
        class GivenVideoMissing {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("{\"items\": []}"));
            }

            @Test
            @DisplayName("Then - true를 반환한다")
            void thenReturnsTrue() {
                assertThat(sut.isBlocked("abc")).isTrue();
            }
        }

        @Nested
        @DisplayName("Given - embeddable=true 응답")
        class GivenEmbeddable {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(jsonResponse("""
                        { "items": [{ "status": { "embeddable": true } }] }
                        """));
            }

            @Test
            @DisplayName("Then - false를 반환한다")
            void thenReturnsFalse() {
                assertThat(sut.isBlocked("abc")).isFalse();
            }
        }

        @Nested
        @DisplayName("Given - API 호출 예외")
        class GivenApiException {
            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(
                        new MockResponse.Builder().code(500).body("boom").build());
            }

            @Test
            @DisplayName("Then - false를 반환한다")
            void thenReturnsFalse() {
                assertThat(sut.isBlocked("abc")).isFalse();
            }
        }
    }

    private MockResponse jsonResponse(String body) {
        return new MockResponse.Builder()
                .code(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build();
    }
}
