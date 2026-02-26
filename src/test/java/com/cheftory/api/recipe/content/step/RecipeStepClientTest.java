package com.cheftory.api.recipe.content.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.cheftory.api.recipe.content.step.client.RecipeStepExternalClient;
import com.cheftory.api.recipe.content.step.client.RecipeStepHttpApi;
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
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

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
        RecipeStepHttpApi recipeStepHttpApi = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build()
                .createClient(RecipeStepHttpApi.class);

        recipeStepClient = new RecipeStepExternalClient(recipeStepHttpApi);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("레시피 단계 조회 (fetch)")
    class Fetch {

        @Nested
        @DisplayName("Given - 정상적인 응답이 주어졌을 때")
        class GivenSuccessResponse {
            String fileUri;
            String mimeType;
            String responseJson;

            @BeforeEach
            void setUp() {
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
                responseJson = """
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
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFetching {
                ClientRecipeStepsResponse result;

                @BeforeEach
                void setUp() throws Exception {
                    result = recipeStepClient.fetch(fileUri, mimeType);
                }

                @Test
                @DisplayName("Then - 올바른 레시피 단계 목록을 반환한다")
                void thenReturnsCorrectSteps() throws Exception {
                    assertThat(result).isNotNull();
                    assertThat(result.steps()).hasSize(2);

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
            }
        }

        @Nested
        @DisplayName("Given - 서버 에러 응답이 주어졌을 때")
        class GivenErrorResponse {
            String fileUri;
            String mimeType;

            @BeforeEach
            void setUp() {
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";

                mockWebServer.enqueue(new MockResponse.Builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"error\":\"Invalid request\"}")
                        .build());
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFetching {
                Throwable thrown;

                @BeforeEach
                void setUp() {
                    thrown = catchThrowable(() -> recipeStepClient.fetch(fileUri, mimeType));
                }

                @Test
                @DisplayName("Then - RecipeStepException 예외를 던진다")
                void thenThrowsException() throws InterruptedException {
                    assertThat(thrown)
                            .isInstanceOf(RecipeStepException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeStepErrorCode.RECIPE_STEP_CREATE_FAIL);

                    RecordedRequest recordedRequest = mockWebServer.takeRequest();
                    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 단계 목록 응답이 주어졌을 때")
        class GivenEmptyResponse {
            String fileUri;
            String mimeType;

            @BeforeEach
            void setUp() {
                fileUri = "s3://bucket/empty.mp4";
                mimeType = "video/mp4";

                mockWebServer.enqueue(new MockResponse.Builder()
                        .code(HttpStatus.OK.value())
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"steps\": []}")
                        .build());
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFetching {
                ClientRecipeStepsResponse result;

                @BeforeEach
                void setUp() throws Exception {
                    result = recipeStepClient.fetch(fileUri, mimeType);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmptyList() {
                    assertThat(result).isNotNull();
                    assertThat(result.steps()).isEmpty();
                }
            }
        }

        @Nested
        @DisplayName("Given - 특수문자가 포함된 응답이 주어졌을 때")
        class GivenSpecialCharResponse {
            String fileUri;
            String mimeType;
            String responseJson;

            @BeforeEach
            void setUp() {
                fileUri = "s3://bucket/special.mp4";
                mimeType = "video/mp4";
                responseJson = """
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
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFetching {
                ClientRecipeStepsResponse result;

                @BeforeEach
                void setUp() throws Exception {
                    result = recipeStepClient.fetch(fileUri, mimeType);
                }

                @Test
                @DisplayName("Then - 특수문자를 포함한 데이터를 정상적으로 반환한다")
                void thenReturnsCorrectData() {
                    assertThat(result.steps()).hasSize(1);
                    ClientRecipeStepsResponse.Step step = result.steps().getFirst();
                    assertThat(step.subtitle()).isEqualTo("특수 단계 & 테스트");
                    assertThat(step.start()).isEqualTo(0.123);
                }
            }
        }
    }
}
