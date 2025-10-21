package com.cheftory.api.recipeinfo.youtubemeta.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.recipeinfo.youtubemeta.YoutubeUri;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeVideoInfo;
import java.io.IOException;
import java.net.URI;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("VideoInfoClient")
public class VideoInfoClientTest {

  private MockWebServer mockWebServer;
  private VideoInfoClient videoInfoClient;
  private static final String YOUTUBE_API_KEY = "test-api-key";

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

    videoInfoClient = new VideoInfoClient(webClient);
    ReflectionTestUtils.setField(videoInfoClient, "YOUTUBE_KEY", YOUTUBE_API_KEY);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @DisplayName("비디오 정보 조회")
  @Nested
  class FetchVideoInfo {

    @Nested
    @DisplayName("Given - 유효한 YouTube URL이 주어졌을 때")
    class GivenValidYoutubeUrl {

      private YoutubeUri youtubeUri;
      private String videoId;

      @BeforeEach
      void setUp() {
        videoId = "dQw4w9WgXcQ";
        youtubeUri = mock(YoutubeUri.class);
      }

      @Nested
      @DisplayName("When - YouTube API가 성공 응답을 반환하면")
      class WhenYoutubeApiReturnsSuccess {

        @BeforeEach
        void setUp() {
          String responseBody =
              """
              {
                "items": [
                  {
                    "snippet": {
                      "title": "맛있는 김치찌개 만들기",
                      "thumbnails": {
                        "default": {
                          "url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/default.jpg",
                          "width": 120,
                          "height": 90
                        },
                        "medium": {
                          "url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/mqdefault.jpg",
                          "width": 320,
                          "height": 180
                        },
                        "high": {
                          "url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
                          "width": 480,
                          "height": 360
                        },
                        "maxres": {
                          "url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
                          "width": 1280,
                          "height": 720
                        }
                      }
                    },
                    "contentDetails": {
                      "duration": "PT10M30S"
                    }
                  }
                ]
              }
              """;

          mockWebServer.enqueue(
              new MockResponse()
                  .setResponseCode(200)
                  .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .setBody(responseBody));

          doReturn(videoId).when(youtubeUri).getVideoId();
          doReturn(URI.create("https://www.youtube.com/watch?v=" + videoId))
              .when(youtubeUri)
              .getNormalizedUrl();
        }

        @Test
        @DisplayName("Then - 비디오 정보가 정상적으로 반환된다")
        void thenReturnsYoutubeVideoInfo() throws InterruptedException {
          YoutubeVideoInfo result = videoInfoClient.fetchVideoInfo(youtubeUri);

          assertThat(result).isNotNull();
          assertThat(result.getVideoUri()).isEqualTo(youtubeUri.getNormalizedUrl());
          assertThat(result.getTitle()).isEqualTo("맛있는 김치찌개 만들기");
          assertThat(result.getThumbnailUrl())
              .isEqualTo(URI.create("https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"));
          assertThat(result.getVideoSeconds()).isEqualTo(630);
          assertThat(result.getVideoId()).isEqualTo(videoId);

          RecordedRequest recordedRequest = mockWebServer.takeRequest();
          assertThat(recordedRequest.getMethod()).isEqualTo("GET");
          assertThat(recordedRequest.getPath())
              .contains("/videos")
              .contains("id=" + videoId)
              .contains("key=" + YOUTUBE_API_KEY)
              .contains("part=snippet,contentDetails");
        }
      }
    }
  }

  @DisplayName("비디오 차단 여부 확인")
  @Nested
  class IsBlockedVideo {

    private YoutubeUri youtubeUri;
    private String videoId;

    @BeforeEach
    void setUp() {
      videoId = "abcd1234";
      youtubeUri = mock(YoutubeUri.class);
      doReturn(videoId).when(youtubeUri).getVideoId();
    }

    @Test
    @DisplayName("items가 비어있으면 true (차단됨)")
    void returnsTrueWhenItemsEmpty() {
      String responseBody =
          """
          {
            "items": []
          }
          """;

      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody(responseBody));

      Boolean result = videoInfoClient.isBlockedVideo(youtubeUri);
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("items가 존재하면 false (차단 아님)")
    void returnsFalseWhenItemsPresent() {
      String responseBody =
          """
          {
            "items": [ { "id": "abcd1234" } ]
          }
          """;

      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody(responseBody));

      Boolean result = videoInfoClient.isBlockedVideo(youtubeUri);
      assertThat(result).isFalse();
    }
  }
}
