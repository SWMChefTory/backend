package com.cheftory.api.recipe.content.briefing.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
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
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("BriefingExternalClient 테스트")
public class BriefingExternalClientTest {

    private MockWebServer mockWebServer;
    private BriefingExternalClient briefingExternalClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        briefingExternalClient = new BriefingExternalClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("브리핑 조회 (fetchBriefing)")
    class FetchBriefing {

        @Nested
        @DisplayName("Given - 유효한 비디오 ID가 주어졌을 때")
        class GivenValidVideoId {
            String videoId;

            @BeforeEach
            void setUp() {
                videoId = "valid-video-id";
            }

            @Nested
            @DisplayName("When - 서버가 성공 응답을 반환하면")
            class WhenSuccess {
                BriefingClientResponse result;

                @BeforeEach
                void setUp() throws Exception {
                    BriefingClientResponse expectedResponse = new BriefingClientResponse(
                            List.of("이 요리는 매우 맛있습니다", "조리 시간이 30분 정도 걸립니다", "초보자도 쉽게 따라할 수 있어요"));

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(objectMapper.writeValueAsString(expectedResponse))
                            .build());

                    result = briefingExternalClient.fetchBriefing(videoId);
                }

                @Test
                @DisplayName("Then - 브리핑 응답을 반환한다")
                void thenReturnsResponse() throws InterruptedException {
                    assertThat(result).isNotNull();
                    assertThat(result.briefings()).hasSize(3);
                    assertThat(result.briefings())
                            .containsExactly("이 요리는 매우 맛있습니다", "조리 시간이 30분 정도 걸립니다", "초보자도 쉽게 따라할 수 있어요");

                    RecordedRequest recordedRequest = mockWebServer.takeRequest();
                    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
                    assertThat(recordedRequest.getTarget()).isEqualTo("/briefings");
                    assertThat(recordedRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE))
                            .contains(MediaType.APPLICATION_JSON_VALUE);

                    String requestBody = recordedRequest.getBody().utf8();
                    assertThat(requestBody).contains("\"video_id\":\"" + videoId + "\"");
                }
            }

            @Nested
            @DisplayName("When - 서버가 빈 목록을 반환하면")
            class WhenEmptyList {
                BriefingClientResponse result;

                @BeforeEach
                void setUp() throws Exception {
                    BriefingClientResponse expectedResponse = new BriefingClientResponse(List.of());

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(objectMapper.writeValueAsString(expectedResponse))
                            .build());

                    result = briefingExternalClient.fetchBriefing(videoId);
                }

                @Test
                @DisplayName("Then - 빈 브리핑 목록을 반환한다")
                void thenReturnsEmptyList() {
                    assertThat(result).isNotNull();
                    assertThat(result.briefings()).isEmpty();
                }
            }
        }

        @Nested
        @DisplayName("Given - 비디오 ID가 null일 때")
        class GivenNullVideoId {

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFetching {

                @Test
                @DisplayName("Then - NullPointerException을 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> briefingExternalClient.fetchBriefing(null))
                            .isInstanceOf(NullPointerException.class)
                            .hasMessage("videoId는 null일 수 없습니다.");
                }
            }
        }

        @Nested
        @DisplayName("Given - 서버 오류가 발생했을 때")
        class GivenServerError {
            String videoId;

            @BeforeEach
            void setUp() {
                videoId = "error-video-id";
            }

            @Nested
            @DisplayName("When - 500 에러가 발생하면")
            class When500Error {

                @BeforeEach
                void setUp() {
                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(500)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body("{\"error\":\"Internal Server Error\"}")
                            .build());
                }

                @Test
                @DisplayName("Then - RecipeBriefingException을 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> briefingExternalClient.fetchBriefing(videoId))
                            .isInstanceOf(RecipeBriefingException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
                }
            }

            @Nested
            @DisplayName("When - 400 에러가 발생하면")
            class When400Error {

                @BeforeEach
                void setUp() {
                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(400)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body("{\"error\":\"Bad Request\"}")
                            .build());
                }

                @Test
                @DisplayName("Then - RecipeBriefingException을 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> briefingExternalClient.fetchBriefing(videoId))
                            .isInstanceOf(RecipeBriefingException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
                }
            }

            @Nested
            @DisplayName("When - 네트워크 연결이 실패하면")
            class WhenNetworkFail {

                @BeforeEach
                void setUp() throws IOException {
                    mockWebServer.close();
                }

                @Test
                @DisplayName("Then - RecipeBriefingException을 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> briefingExternalClient.fetchBriefing(videoId))
                            .isInstanceOf(RecipeBriefingException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
                }
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 JSON 응답이 왔을 때")
        class GivenInvalidJson {
            String videoId;

            @BeforeEach
            void setUp() {
                videoId = "invalid-json-video-id";
                mockWebServer.enqueue(new MockResponse.Builder()
                        .code(200)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("{ invalid json }")
                        .build());
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFetching {

                @Test
                @DisplayName("Then - RecipeBriefingException을 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> briefingExternalClient.fetchBriefing(videoId))
                            .isInstanceOf(RecipeBriefingException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
                }
            }
        }
    }
}
