package com.cheftory.api.recipeinfo.caption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cheftory.api.recipeinfo.caption.client.CaptionClient;
import com.cheftory.api.recipeinfo.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipeinfo.caption.client.exception.CaptionClientErrorCode;
import com.cheftory.api.recipeinfo.caption.client.exception.CaptionClientException;
import com.cheftory.api.recipeinfo.caption.entity.LangCodeType;
import java.io.IOException;
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
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("CaptionClient")
public class RecipeCaptionClientTest {

  private MockWebServer mockWebServer;
  private CaptionClient captionClient;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

    captionClient = new CaptionClient(webClient);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @DisplayName("자막 정보 조회")
  @Nested
  class FetchCaption {

    @Nested
    @DisplayName("Given - 유효한 비디오 ID가 주어졌을 때")
    class GivenValidVideoId {

      private String videoId;

      @BeforeEach
      void setUp() {
        videoId = "sample-video-id";
        // expectedResponse는 실제 검증에서만 사용
      }

      @Nested
      @DisplayName("When - API가 성공 응답을 반환하면")
      class WhenApiReturnsSuccess {

        @BeforeEach
        void setUp() {
          // JSON 응답 직접 작성
          String responseBody =
              """
              {
                "lang_code": "ko",
                "captions": [
                  {
                    "start": 0.0,
                    "end": 2.0,
                    "text": "안녕하세요"
                  },
                  {
                    "start": 2.0,
                    "end": 4.0,
                    "text": "요리를 시작하겠습니다"
                  }
                ]
              }
              """;

          mockWebServer.enqueue(
              new MockResponse()
                  .setResponseCode(200)
                  .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .setBody(responseBody));
        }

        @Test
        @DisplayName("Then - 자막 정보가 정상적으로 반환된다")
        void thenReturnsClientCaptionResponse() throws InterruptedException {
          // when
          ClientCaptionResponse result = captionClient.fetchCaption(videoId);

          // then
          assertThat(result).isNotNull();
          assertThat(result.langCode()).isEqualTo(LangCodeType.ko);
          assertThat(result.segments()).hasSize(2);

          // 요청 검증
          RecordedRequest recordedRequest = mockWebServer.takeRequest();
          assertThat(recordedRequest.getMethod()).isEqualTo("POST");
          assertThat(recordedRequest.getPath()).isEqualTo("/captions");
          assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE))
              .contains(MediaType.APPLICATION_JSON_VALUE);
        }
      }
    }

    @Nested
    @DisplayName("Given - API가 4xx 클라이언트 오류를 반환할 때")
    class GivenApi4xxError {

      private String videoId;

      @BeforeEach
      void setUp() {
        videoId = "invalid-video-id";
      }

      @Nested
      @DisplayName("When - CAPTION_001 오류가 발생하면")
      class WhenCaptionNotFound {

        @BeforeEach
        void setUp() {
          String errorResponseBody =
              """
              {
                "error_code": "CAPTION_001",
                "error_message": "자막을 찾을 수 없습니다"
              }
              """;

          mockWebServer.enqueue(
              new MockResponse()
                  .setResponseCode(404)
                  .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .setBody(errorResponseBody));
        }

        @Test
        @DisplayName("Then - NOT_COOK_VIDEO 예외가 발생한다")
        void thenThrowsNotCookVideoException() {
          CaptionClientException exception =
              assertThrows(CaptionClientException.class, () -> captionClient.fetchCaption(videoId));
          assertThat(exception.getErrorMessage()).isEqualTo(CaptionClientErrorCode.NOT_COOK_VIDEO);
        }
      }
    }

    @Nested
    @DisplayName("Given - API가 5xx 서버 오류를 반환할 때")
    class GivenApi5xxError {

      private String videoId;

      @BeforeEach
      void setUp() {
        videoId = "test-video-id";
      }

      @Nested
      @DisplayName("When - 서버 내부 오류가 발생하면")
      class WhenInternalServerError {

        @BeforeEach
        void setUp() {
          String errorResponseBody =
              """
              {
                "error_code": "SERVER_001",
                "error_message": "서버 내부 오류"
              }
              """;

          mockWebServer.enqueue(
              new MockResponse()
                  .setResponseCode(500)
                  .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .setBody(errorResponseBody));
        }

        @Test
        @DisplayName("Then - SERVER_ERROR 예외가 발생한다")
        void thenThrowsServerErrorException() {
          // when & then
          CaptionClientException exception =
              assertThrows(CaptionClientException.class, () -> captionClient.fetchCaption(videoId));
          assertThat(exception.getErrorMessage()).isEqualTo(CaptionClientErrorCode.SERVER_ERROR);
        }
      }
    }
  }
}
