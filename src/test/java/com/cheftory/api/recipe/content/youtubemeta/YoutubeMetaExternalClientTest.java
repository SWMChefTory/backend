package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaExternalClient;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.io.IOException;
import java.net.URI;
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

@DisplayName("YoutubeMetaExternalClient 테스트")
public class YoutubeMetaExternalClientTest {

    private MockWebServer mockWebServer;
    private YoutubeMetaExternalClient youtubeMetaExternalClient;
    private static final String YOUTUBE_API_DEFAULT_KEY = "test-api-default-key";
    private static final String YOUTUBE_API_BLOCK_CHECK_KEY = "test-api-block-check-key";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        youtubeMetaExternalClient = new YoutubeMetaExternalClient(webClient);
        ReflectionTestUtils.setField(youtubeMetaExternalClient, "youtubeDefaultKey", YOUTUBE_API_DEFAULT_KEY);
        ReflectionTestUtils.setField(youtubeMetaExternalClient, "youtubeBlockCheckKey", YOUTUBE_API_BLOCK_CHECK_KEY);
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("비디오 정보 조회 (fetch)")
    class Fetch {

        @Nested
        @DisplayName("Given - 유효한 YouTube URL이 주어졌을 때")
        class GivenValidUrl {
            YoutubeUri youtubeUri;
            String videoId;

            @BeforeEach
            void setUp() {
                videoId = "dQw4w9WgXcQ";
                youtubeUri = mock(YoutubeUri.class);
                doReturn(videoId).when(youtubeUri).getVideoId();
                doReturn(URI.create("https://www.youtube.com/watch?v=" + videoId))
                        .when(youtubeUri)
                        .getNormalizedUrl();
            }

            @Nested
            @DisplayName("When - API가 성공 응답을 반환하면")
            class WhenApiSuccess {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": [
                                    {
                                        "snippet": {
                                            "title": "맛있는 김치찌개 만들기",
                                            "channelTitle": "맛있는 집밥 채널",
                                            "thumbnails": {
                                                "maxres": {
                                                    "url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
                                                }
                                            }
                                        },
                                        "contentDetails": {
                                            "duration": "PT10M30S"
                                        },
                                        "status": {
                                            "embeddable": true
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - 비디오 정보를 반환한다")
                void thenReturnsInfo() throws Exception {
                    YoutubeVideoInfo result = youtubeMetaExternalClient.fetch(youtubeUri);

                    assertThat(result).isNotNull();
                    assertThat(result.getVideoUri()).isEqualTo(youtubeUri.getNormalizedUrl());
                    assertThat(result.getTitle()).isEqualTo("맛있는 김치찌개 만들기");
                    assertThat(result.getVideoType()).isEqualTo(YoutubeMetaType.NORMAL);

                    RecordedRequest recordedRequest = mockWebServer.takeRequest();
                    assertThat(recordedRequest.getMethod()).isEqualTo("GET");
                    assertThat(recordedRequest.getTarget()).contains("/videos").contains("id=" + videoId);
                }
            }

            @Nested
            @DisplayName("When - API가 빈 items를 반환하면")
            class WhenApiEmpty {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": []
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - VIDEO_NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> youtubeMetaExternalClient.fetch(youtubeUri))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_FOUND);
                }
            }

            @Nested
            @DisplayName("When - embeddable이 false면")
            class WhenNotEmbeddable {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": [
                                    {
                                        "snippet": {
                                            "title": "임베드 불가 비디오",
                                            "channelTitle": "채널",
                                            "thumbnails": {
                                                "maxres": {
                                                    "url": "https://i.ytimg.com/vi/test/maxresdefault.jpg"
                                                }
                                            }
                                        },
                                        "contentDetails": {
                                            "duration": "PT5M"
                                        },
                                        "status": {
                                            "embeddable": false
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - VIDEO_NOT_EMBEDDABLE 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> youtubeMetaExternalClient.fetch(youtubeUri))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue(
                                    "error", YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_EMBEDDABLE);
                }
            }

            @Nested
            @DisplayName("When - duration이 null이면")
            class WhenDurationNull {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": [
                                    {
                                        "snippet": {
                                            "title": "duration 없는 비디오",
                                            "channelTitle": "채널",
                                            "thumbnails": {
                                                "maxres": {
                                                    "url": "https://i.ytimg.com/vi/test/maxresdefault.jpg"
                                                }
                                            }
                                        },
                                        "contentDetails": {},
                                        "status": {
                                            "embeddable": true
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - VIDEO_DURATION_NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> youtubeMetaExternalClient.fetch(youtubeUri))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue(
                                    "error", YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_DURATION_NOT_FOUND);
                }
            }

            @Nested
            @DisplayName("When - 60초 이하 비디오면")
            class WhenShortsByDuration {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": [
                                    {
                                        "snippet": {
                                            "title": "쇼츠 비디오",
                                            "channelTitle": "쇼츠 채널",
                                            "thumbnails": {
                                                "maxres": {
                                                    "url": "https://i.ytimg.com/vi/test/maxresdefault.jpg"
                                                }
                                            }
                                        },
                                        "contentDetails": {
                                            "duration": "PT45S"
                                        },
                                        "status": {
                                            "embeddable": true
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - SHORTS 타입으로 반환한다")
                void thenReturnsShorts() throws Exception {
                    YoutubeVideoInfo result = youtubeMetaExternalClient.fetch(youtubeUri);

                    assertThat(result).isNotNull();
                    assertThat(result.getVideoType()).isEqualTo(YoutubeMetaType.SHORTS);
                }
            }

            @Nested
            @DisplayName("When - API 호출이 실패하면")
            class WhenApiError {

                @BeforeEach
                void setUp() {
                    mockWebServer.enqueue(new MockResponse.Builder().code(500).build());
                }

                @Test
                @DisplayName("Then - API_ERROR 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> youtubeMetaExternalClient.fetch(youtubeUri))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_API_ERROR);
                }
            }

            @Nested
            @DisplayName("When - channelId가 있고 쇼츠 플레이리스트에 있으면")
            class WhenShortsInPlaylist {

                @BeforeEach
                void setUp() {
                    // 메인 비디오 응답
                    String videoResponseBody =
                            """
                            {
                                "items": [
                                    {
                                        "snippet": {
                                            "title": "플레이리스트 쇼츠",
                                            "channelTitle": "채널",
                                            "channelId": "UC123456789",
                                            "thumbnails": {
                                                "maxres": {
                                                    "url": "https://i.ytimg.com/vi/test/maxresdefault.jpg"
                                                }
                                            }
                                        },
                                        "contentDetails": {
                                            "duration": "PT2M"
                                        },
                                        "status": {
                                            "embeddable": true
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(videoResponseBody)
                            .build());

                    // 쇼츠 플레이리스트 응답 (비디오가 있음)
                    String playlistResponseBody =
                            """
                            {
                                "items": [
                                    {
                                        "snippet": {
                                            "title": "쇼츠"
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(playlistResponseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - SHORTS 타입으로 반환한다")
                void thenReturnsShorts() throws Exception {
                    YoutubeVideoInfo result = youtubeMetaExternalClient.fetch(youtubeUri);

                    assertThat(result).isNotNull();
                    assertThat(result.getVideoType()).isEqualTo(YoutubeMetaType.SHORTS);
                }
            }

            @Nested
            @DisplayName("When - 쇼츠 플레이리스트 API가 실패하면")
            class WhenShortsPlaylistApiFails {

                @BeforeEach
                void setUp() {
                    // 메인 비디오 응답 (2분 - 60초 초과)
                    String videoResponseBody =
                            """
                            {
                                "items": [
                                    {
                                        "snippet": {
                                            "title": "일반 비디오",
                                            "channelTitle": "채널",
                                            "channelId": "UC123456789",
                                            "thumbnails": {
                                                "maxres": {
                                                    "url": "https://i.ytimg.com/vi/test/maxresdefault.jpg"
                                                }
                                            }
                                        },
                                        "contentDetails": {
                                            "duration": "PT2M"
                                        },
                                        "status": {
                                            "embeddable": true
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(videoResponseBody)
                            .build());

                    // 쇼츠 플레이리스트 API 실패
                    mockWebServer.enqueue(new MockResponse.Builder().code(500).build());
                }

                @Test
                @DisplayName("Then - NORMAL 타입으로 반환한다 (fallback)")
                void thenReturnsNormal() throws Exception {
                    YoutubeVideoInfo result = youtubeMetaExternalClient.fetch(youtubeUri);

                    assertThat(result).isNotNull();
                    assertThat(result.getVideoType()).isEqualTo(YoutubeMetaType.NORMAL);
                }
            }
        }
    }

    @Nested
    @DisplayName("비디오 차단 여부 확인 (isBlocked)")
    class IsBlocked {

        @Nested
        @DisplayName("Given - 비디오 ID가 주어졌을 때")
        class GivenVideoId {
            YoutubeUri youtubeUri;
            String videoId;

            @BeforeEach
            void setUp() {
                videoId = "abcd1234";
                youtubeUri = mock(YoutubeUri.class);
                doReturn(videoId).when(youtubeUri).getVideoId();
            }

            @Nested
            @DisplayName("When - API 응답이 비어있으면")
            class WhenApiEmpty {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": []
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - true(차단됨)를 반환한다")
                void thenReturnsTrue() {
                    Boolean result = youtubeMetaExternalClient.isBlocked(youtubeUri);
                    assertThat(result).isTrue();
                }
            }

            @Nested
            @DisplayName("When - API 응답이 존재하면")
            class WhenApiPresent {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": [
                                    {
                                        "status": {
                                            "embeddable": true
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - false(차단 안됨)를 반환한다")
                void thenReturnsFalse() {
                    Boolean result = youtubeMetaExternalClient.isBlocked(youtubeUri);
                    assertThat(result).isFalse();
                }
            }

            @Nested
            @DisplayName("When - embeddable이 false면")
            class WhenEmbeddableFalse {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": [
                                    {
                                        "status": {
                                            "embeddable": false
                                        }
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - true(차단됨)를 반환한다")
                void thenReturnsTrue() {
                    Boolean result = youtubeMetaExternalClient.isBlocked(youtubeUri);
                    assertThat(result).isTrue();
                }
            }

            @Nested
            @DisplayName("When - embeddable이 null이면")
            class WhenEmbeddableNull {

                @BeforeEach
                void setUp() {
                    String responseBody =
                            """
                            {
                                "items": [
                                    {
                                        "status": {}
                                    }
                                ]
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build());
                }

                @Test
                @DisplayName("Then - true(차단됨)를 반환한다")
                void thenReturnsTrue() {
                    Boolean result = youtubeMetaExternalClient.isBlocked(youtubeUri);
                    assertThat(result).isTrue();
                }
            }

            @Nested
            @DisplayName("When - API 호출이 실패하면")
            class WhenApiError {

                @BeforeEach
                void setUp() {
                    mockWebServer.enqueue(new MockResponse.Builder().code(500).build());
                }

                @Test
                @DisplayName("Then - false를 반환한다")
                void thenReturnsFalse() {
                    Boolean result = youtubeMetaExternalClient.isBlocked(youtubeUri);
                    assertThat(result).isFalse();
                }
            }
        }
    }
}
