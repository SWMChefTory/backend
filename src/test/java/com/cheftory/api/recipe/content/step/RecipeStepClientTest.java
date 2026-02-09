package com.cheftory.api.recipe.content.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipe.content.step.client.RecipeStepExternalClient;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsRequest;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.content.step.exception.RecipeStepErrorCode;
import com.cheftory.api.recipe.content.step.exception.RecipeStepException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("RecipeStepExternalClient 테스트")
class RecipeStepClientTest {

    private MockWebServer mockWebServer;
    private RecipeStepExternalClient recipeStepClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        recipeStepClient = new RecipeStepExternalClient(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
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

            mockWebServer.enqueue(new MockResponse.Builder()
                    .code(HttpStatus.OK.value())
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(responseJson)
                    .build());

            ClientRecipeStepsResponse actualResponse = recipeStepClient.fetch(fileUri, mimeType);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.steps()).hasSize(2);

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getTarget()).isEqualTo("/steps/video");
            assertThat(recordedRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE))
                    .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

            ClientRecipeStepsRequest actualRequest =
                    objectMapper.readValue(recordedRequest.getBody().utf8(), ClientRecipeStepsRequest.class);
            assertThat(actualRequest.fileUri()).isEqualTo(fileUri);
            assertThat(actualRequest.mimeType()).isEqualTo(mimeType);
        }

        @Test
        @DisplayName("서버 에러 시 RecipeStepException으로 래핑된다")
        void shouldThrowRecipeStepExceptionOnServerError() throws Exception {
            String fileUri = "s3://bucket/file.mp4";
            String mimeType = "video/mp4";

            mockWebServer.enqueue(new MockResponse.Builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"error\":\"Invalid request\"}")
                    .build());

            assertThatThrownBy(() -> recipeStepClient.fetch(fileUri, mimeType))
                    .isInstanceOf(RecipeStepException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeStepErrorCode.RECIPE_STEP_CREATE_FAIL);

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        }

        @Test
        @DisplayName("빈 단계 목록이 반환된다")
        void shouldReturnEmptyStepsWhenEmptyResponse() throws Exception {
            String fileUri = "s3://bucket/empty.mp4";
            String mimeType = "video/mp4";

            mockWebServer.enqueue(new MockResponse.Builder()
                    .code(HttpStatus.OK.value())
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"steps\": []}")
                    .build());

            ClientRecipeStepsResponse actualResponse = recipeStepClient.fetch(fileUri, mimeType);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.steps()).isEmpty();
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

            mockWebServer.enqueue(new MockResponse.Builder()
                    .code(HttpStatus.OK.value())
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(responseJson)
                    .build());

            ClientRecipeStepsResponse actualResponse = recipeStepClient.fetch(fileUri, mimeType);

            assertThat(actualResponse.steps()).hasSize(1);
            ClientRecipeStepsResponse.Step step = actualResponse.steps().getFirst();
            assertThat(step.subtitle()).isEqualTo("특수 단계 & 테스트");
            assertThat(step.start()).isEqualTo(0.123);
        }
    }
}
