package com.cheftory.api.recipe.content.briefing.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
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

@DisplayName("BriefingClient 테스트")
public class BriefingClientTest {

    private MockWebServer mockWebServer;
    private BriefingClient briefingClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        briefingClient = new BriefingClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("브리핑 조회")
    class FetchBriefing {

        @Nested
        @DisplayName("Given - 유효한 비디오 ID가 주어졌을 때")
        class GivenValidVideoId {

            private String videoId;

            @BeforeEach
            void setUp() {
                videoId = "valid-video-id";
            }

            @Nested
            @DisplayName("When - 서버가 성공적인 응답을 반환하면")
            class WhenServerReturnsSuccessResponse {

                @BeforeEach
                void setUp() throws Exception {
                    BriefingClientResponse expectedResponse = new BriefingClientResponse(
                            List.of("이 요리는 매우 맛있습니다", "조리 시간이 30분 정도 걸립니다", "초보자도 쉽게 따라할 수 있어요"));

                    mockWebServer.enqueue(new MockResponse()
                            .setResponseCode(200)
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody(objectMapper.writeValueAsString(expectedResponse)));
                }

                @DisplayName("Then - 브리핑 응답이 성공적으로 반환된다")
                @Test
                void shouldReturnBriefingResponseSuccessfully() throws InterruptedException, RecipeBriefingException {
                    BriefingClientResponse result = briefingClient.fetchBriefing(videoId);

                    assertThat(result).isNotNull();
                    assertThat(result.briefings()).hasSize(3);
                    assertThat(result.briefings())
                            .containsExactly("이 요리는 매우 맛있습니다", "조리 시간이 30분 정도 걸립니다", "초보자도 쉽게 따라할 수 있어요");

                    // 요청이 올바르게 전송되었는지 확인
                    RecordedRequest recordedRequest = mockWebServer.takeRequest();
                    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
                    assertThat(recordedRequest.getPath()).isEqualTo("/briefings");
                    assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE))
                            .contains(MediaType.APPLICATION_JSON_VALUE);

                    // 요청 본문 확인
                    String requestBody = recordedRequest.getBody().readUtf8();
                    assertThat(requestBody).contains("\"video_id\":\"" + videoId + "\"");
                }
            }

            @Nested
            @DisplayName("When - 서버가 빈 브리핑 목록을 반환하면")
            class WhenServerReturnsEmptyBriefings {

                @BeforeEach
                void setUp() throws Exception {
                    BriefingClientResponse expectedResponse = new BriefingClientResponse(List.of());

                    mockWebServer.enqueue(new MockResponse()
                            .setResponseCode(200)
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody(objectMapper.writeValueAsString(expectedResponse)));
                }

                @DisplayName("Then - 빈 브리핑 목록이 반환된다")
                @Test
                void shouldReturnEmptyBriefingList() throws RecipeBriefingException {
                    BriefingClientResponse result = briefingClient.fetchBriefing(videoId);

                    assertThat(result).isNotNull();
                    assertThat(result.briefings()).isEmpty();
                }
            }
        }

        @Nested
        @DisplayName("Given - null 비디오 ID가 주어졌을 때")
        class GivenNullVideoId {

            @Nested
            @DisplayName("When - 브리핑을 조회하면")
            class WhenFetchBriefing {
                @Test
                @DisplayName("Then - NullPointerException이 발생한다")
                void shouldThrowNullPointerException() throws Exception {
                    assertThatThrownBy(() -> briefingClient.fetchBriefing(null))
                            .isInstanceOf(NullPointerException.class)
                            .hasMessage("videoId는 null일 수 없습니다.");
                }
            }
        }

        @Nested
        @DisplayName("Given - 서버 오류가 발생할 때")
        class GivenServerError {

            private String videoId;

            @BeforeEach
            void setUp() {
                videoId = "error-video-id";
            }

            @Nested
            @DisplayName("When - 서버가 500 에러를 반환하면")
            class WhenServerReturns500Error {

                @BeforeEach
                void setUp() {
                    mockWebServer.enqueue(new MockResponse()
                            .setResponseCode(500)
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody("{\"error\":\"Internal Server Error\"}"));
                }

                @Test
                @DisplayName("Then - RecipeBriefingException이 발생한다")
                void shouldThrowBriefingException() throws Exception {
                    assertThatThrownBy(() -> briefingClient.fetchBriefing(videoId))
                            .isInstanceOf(RecipeBriefingException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
                }
            }

            @Nested
            @DisplayName("When - 서버가 400 에러를 반환하면")
            class WhenServerReturns400Error {

                @BeforeEach
                void setUp() {
                    mockWebServer.enqueue(new MockResponse()
                            .setResponseCode(400)
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody("{\"error\":\"Bad Request\"}"));
                }

                @Test
                @DisplayName("Then - RecipeBriefingException이 발생한다")
                void shouldThrowBriefingException() throws Exception {
                    assertThatThrownBy(() -> briefingClient.fetchBriefing(videoId))
                            .isInstanceOf(RecipeBriefingException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
                }
            }

            @Nested
            @DisplayName("When - 네트워크 연결이 실패하면")
            class WhenNetworkConnectionFails {

                @BeforeEach
                void setUp() throws IOException {
                    // MockWebServer를 종료하여 연결 실패 상황 시뮬레이션
                    mockWebServer.shutdown();
                }

                @Test
                @DisplayName("Then - RecipeBriefingException이 발생한다")
                void shouldThrowBriefingException() throws Exception {
                    assertThatThrownBy(() -> briefingClient.fetchBriefing(videoId))
                            .isInstanceOf(RecipeBriefingException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
                }
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 JSON 응답이 반환될 때")
        class GivenInvalidJsonResponse {

            private String videoId;

            @BeforeEach
            void setUp() {
                videoId = "invalid-json-video-id";

                mockWebServer.enqueue(new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("{ invalid json }"));
            }

            @Nested
            @DisplayName("When - 브리핑을 조회하면")
            class WhenFetchBriefing {
                @Test
                @DisplayName("Then - RecipeBriefingException이 발생한다")
                void shouldThrowBriefingException() throws Exception {
                    assertThatThrownBy(() -> briefingClient.fetchBriefing(videoId))
                            .isInstanceOf(RecipeBriefingException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
                }
            }
        }
    }

    @Nested
    @DisplayName("요청 형식 검증")
    class RequestFormatValidation {

        @DisplayName("올바른 요청 형식으로 전송되는지 확인")
        @Test
        void shouldSendCorrectRequestFormat() throws Exception {
            String videoId = "test-video-123";

            // 성공 응답 모킹
            BriefingClientResponse mockResponse = new BriefingClientResponse(List.of("테스트 브리핑"));

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(objectMapper.writeValueAsString(mockResponse)));

            // 요청 실행
            briefingClient.fetchBriefing(videoId);

            // 요청 검증
            RecordedRequest recordedRequest = mockWebServer.takeRequest();

            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo("/briefings");
            assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_JSON_VALUE);

            // 요청 본문이 올바른 JSON 형식인지 확인
            String requestBody = recordedRequest.getBody().readUtf8();
            assertThat(requestBody).isNotEmpty();

            // JSON 파싱 가능한지 확인
            ObjectMapper mapper = new ObjectMapper();
            var requestJson = mapper.readTree(requestBody);
            assertThat(requestJson.get("video_id").asText()).isEqualTo(videoId);
        }
    }
}
