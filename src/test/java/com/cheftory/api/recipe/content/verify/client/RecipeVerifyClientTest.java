package com.cheftory.api.recipe.content.verify.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import java.io.IOException;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.Headers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

@DisplayName("RecipeVerifyClient 테스트")
class RecipeVerifyClientTest {

    private MockWebServer mockWebServer;
    private RecipeVerifyClient verifyClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        RecipeVerifyHttpApi recipeVerifyHttpApi = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build()
                .createClient(RecipeVerifyHttpApi.class);

        verifyClient = new RecipeVerifyExternalClient(recipeVerifyHttpApi, new ObjectMapper());
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("비디오 검증 (verify)")
    class Verify {

        @Nested
        @DisplayName("Given - 유효한 비디오 ID가 주어졌을 때")
        class GivenValidVideoId {
            String videoId;

            @BeforeEach
            void setUp() {
                videoId = "sample-video-id";
            }

            @Nested
            @DisplayName("When - 서버가 성공 응답을 반환하면")
            class WhenSuccess {
                RecipeVerifyClientResponse result;

                @BeforeEach
                void setUp() throws Exception {
                    String responseBody = """
                            {
                              "file_uri": "s3://bucket/file.mp4",
                              "mime_type": "video/mp4"
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse(
                            200, Headers.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE), responseBody));

                    result = verifyClient.verify(videoId);
                }

                @Test
                @DisplayName("Then - 파일 URI와 MIME 타입을 반환한다")
                void thenReturnsResponse() throws Exception {
                    assertThat(result).isNotNull();
                    assertThat(result.fileUri()).isEqualTo("s3://bucket/file.mp4");
                    assertThat(result.mimeType()).isEqualTo("video/mp4");

                    RecordedRequest recordedRequest = mockWebServer.takeRequest();
                    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
                    assertThat(recordedRequest.getTarget()).isEqualTo("/verify");

                    assertThat(recordedRequest.getBody()).isNotNull();
                    var requestNode =
                            objectMapper.readTree(recordedRequest.getBody().utf8());
                    assertThat(requestNode.get("video_id").asString()).isEqualTo(videoId);
                }
            }

            @Nested
            @DisplayName("When - 서버 오류가 발생하면")
            class WhenServerError {

                @BeforeEach
                void setUp() {
                    String errorResponseBody = """
                            {
                              "error_code": "SERVER_001",
                              "error_message": "server error"
                            }
                            """;

                    mockWebServer.enqueue(new MockResponse(
                            500,
                            Headers.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE),
                            errorResponseBody));
                }

                @Test
                @DisplayName("Then - SERVER_ERROR 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> verifyClient.verify(videoId))
                            .isInstanceOf(RecipeVerifyException.class)
                            .extracting("error")
                            .isEqualTo(RecipeVerifyErrorCode.SERVER_ERROR);
                }
            }
        }
    }
}
