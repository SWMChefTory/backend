package com.cheftory.api.recipe.content.verify.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@DisplayName("RecipeVerifyClient")
class RecipeVerifyClientTest {

    private MockWebServer mockWebServer;
    private RecipeVerifyClient recipeVerifyClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        recipeVerifyClient = new RecipeVerifyClient(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("verifyVideo")
    class VerifyVideo {

        @Test
        @DisplayName("정상 응답 시 fileUri와 mimeType을 반환한다")
        void shouldReturnVerifyResponse() throws Exception {
            String videoId = "sample-video-id";

            String responseBody =
                    """
          {
            "file_uri": "s3://bucket/file.mp4",
            "mime_type": "video/mp4"
          }
          """;

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseBody));

            RecipeVerifyClientResponse result = recipeVerifyClient.verifyVideo(videoId);

            assertThat(result).isNotNull();
            assertThat(result.fileUri()).isEqualTo("s3://bucket/file.mp4");
            assertThat(result.mimeType()).isEqualTo("video/mp4");

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo("/verify");

            var requestNode = objectMapper.readTree(recordedRequest.getBody().readUtf8());
            assertThat(requestNode.get("video_id").asText()).isEqualTo(videoId);
        }

        @Test
        @DisplayName("VERIFY_003 오류면 SERVER_ERROR 예외가 발생한다")
        void shouldThrowServerErrorForVerify003() {
            String videoId = "invalid-video-id";

            String errorResponseBody =
                    """
          {
            "error_code": "VERIFY_003",
            "error_message": "not cook"
          }
          """;

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(400)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(errorResponseBody));

            RecipeVerifyException exception =
                    assertThrows(RecipeVerifyException.class, () -> recipeVerifyClient.verifyVideo(videoId));
            assertThat(exception.getError()).isEqualTo(RecipeVerifyErrorCode.SERVER_ERROR);
        }

        @Test
        @DisplayName("서버 오류면 SERVER_ERROR 예외가 발생한다")
        void shouldThrowServerError() {
            String videoId = "server-error-id";

            String errorResponseBody =
                    """
          {
            "error_code": "SERVER_001",
            "error_message": "server error"
          }
          """;

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(errorResponseBody));

            RecipeVerifyException exception =
                    assertThrows(RecipeVerifyException.class, () -> recipeVerifyClient.verifyVideo(videoId));
            assertThat(exception.getError()).isEqualTo(RecipeVerifyErrorCode.SERVER_ERROR);
        }
    }
}
