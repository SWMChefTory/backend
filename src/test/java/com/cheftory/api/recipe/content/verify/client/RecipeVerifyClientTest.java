package com.cheftory.api.recipe.content.verify.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.Headers;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("RecipeVerifyClient")
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

        verifyClient = new RecipeVerifyExternalClient(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
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

            mockWebServer.enqueue(new MockResponse(
                    200, Headers.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE), responseBody));

            RecipeVerifyClientResponse result = verifyClient.verify(videoId);

            assertThat(result).isNotNull();
            assertThat(result.fileUri()).isEqualTo("s3://bucket/file.mp4");
            assertThat(result.mimeType()).isEqualTo("video/mp4");

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getTarget()).isEqualTo("/verify");

					Assertions.assertNotNull(recordedRequest.getBody());
					var requestNode = objectMapper.readTree(recordedRequest.getBody().utf8());
            assertThat(requestNode.get("video_id").asText()).isEqualTo(videoId);
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

            mockWebServer.enqueue(new MockResponse(
                    500, Headers.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE), errorResponseBody));

            RecipeVerifyException exception =
                    assertThrows(RecipeVerifyException.class, () -> verifyClient.verify(videoId));
            assertThat(exception.getError()).isEqualTo(RecipeVerifyErrorCode.SERVER_ERROR);
        }
    }
}
