package com.cheftory.api.recipe.content.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipe.content.step.client.RecipeStepClient;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsRequest;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@DisplayName("RecipeStepClient 테스트")
class RecipeStepClientTest {

    private MockWebServer mockWebServer;
    private RecipeStepClient recipeStepClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        recipeStepClient = new RecipeStepClient(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("레시피 단계 조회")
    class FetchRecipeSteps {

        @Test
        @DisplayName("정상 응답 시 올바른 레시피 단계 목록이 반환된다")
        void shouldReturnCorrectRecipeStepsOnSuccess() throws Exception {
            String fileUri = "s3://bucket/file.mp4";
            String mimeType = "video/mp4";

            String responseJson =
                    """
          {
            "steps": [
              {
                "subtitle": "첫 번째 단계",
                "start": 10.0,
                "descriptions": [
                  {
                    "text": "재료를 준비합니다",
                    "start": 10.5
                  },
                  {
                    "text": "도구를 정리합니다",
                    "start": 11.0
                  }
                ]
              },
              {
                "subtitle": "두 번째 단계",
                "start": 30.0,
                "descriptions": [
                  {
                    "text": "요리를 시작합니다",
                    "start": 30.5
                  }
                ]
              }
            ]
          }
          """;
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseJson));

            ClientRecipeStepsResponse actualResponse = recipeStepClient.fetchRecipeSteps(fileUri, mimeType);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.steps()).hasSize(2);

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo("/steps/video");
            assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

            ClientRecipeStepsRequest actualRequest =
                    objectMapper.readValue(recordedRequest.getBody().readUtf8(), ClientRecipeStepsRequest.class);
            assertThat(actualRequest.fileUri()).isEqualTo(fileUri);
            assertThat(actualRequest.mimeType()).isEqualTo(mimeType);
        }

        @Test
        @DisplayName("서버 에러 시 WebClientResponseException이 발생한다")
        void shouldThrowWebClientResponseExceptionOnServerError() throws InterruptedException {
            String fileUri = "s3://bucket/file.mp4";
            String mimeType = "video/mp4";

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(HttpStatus.BAD_REQUEST.value())
                    .setBody("{\"error\":\"Invalid request\"}"));

            assertThatThrownBy(() -> recipeStepClient.fetchRecipeSteps(fileUri, mimeType))
                    .isInstanceOf(WebClientResponseException.class)
                    .satisfies(exception -> {
                        WebClientResponseException webClientException = (WebClientResponseException) exception;
                        assertThat(webClientException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo("/steps/video");
        }

        @Test
        @DisplayName("빈 단계 목록이 반환된다")
        void shouldReturnEmptyStepsWhenEmptyResponse() throws Exception {
            String fileUri = "s3://bucket/empty.mp4";
            String mimeType = "video/mp4";

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody("{\"steps\": []}"));

            ClientRecipeStepsResponse actualResponse = recipeStepClient.fetchRecipeSteps(fileUri, mimeType);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.steps()).isEmpty();

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo("/steps/video");

            ClientRecipeStepsRequest actualRequest =
                    objectMapper.readValue(recordedRequest.getBody().readUtf8(), ClientRecipeStepsRequest.class);
            assertThat(actualRequest.fileUri()).isEqualTo(fileUri);
            assertThat(actualRequest.mimeType()).isEqualTo(mimeType);
        }

        @Test
        @DisplayName("특수문자/숫자 응답도 정상 처리된다")
        void shouldHandleDtoWithSpecialValues() throws Exception {
            String fileUri = "s3://bucket/special.mp4";
            String mimeType = "video/mp4";

            String responseJson =
                    """
          {
            "steps": [
              {
                "subtitle": "특수 단계 & 테스트",
                "start": 0.123,
                "descriptions": [
                  {
                    "text": "특수문자 포함: @#$%",
                    "start": 0.5
                  }
                ]
              }
            ]
          }
          """;
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseJson));

            ClientRecipeStepsResponse actualResponse = recipeStepClient.fetchRecipeSteps(fileUri, mimeType);

            assertThat(actualResponse.steps()).hasSize(1);
            ClientRecipeStepsResponse.Step step = actualResponse.steps().get(0);
            assertThat(step.subtitle()).isEqualTo("특수 단계 & 테스트");
            assertThat(step.start()).isEqualTo(0.123);
            assertThat(step.descriptions()).hasSize(1);

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            ClientRecipeStepsRequest actualRequest =
                    objectMapper.readValue(recordedRequest.getBody().readUtf8(), ClientRecipeStepsRequest.class);
            assertThat(actualRequest.fileUri()).isEqualTo(fileUri);
            assertThat(actualRequest.mimeType()).isEqualTo(mimeType);
        }
    }
}
