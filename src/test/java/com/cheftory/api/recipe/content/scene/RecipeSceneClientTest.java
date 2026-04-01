package com.cheftory.api.recipe.content.scene;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.recipe.content.scene.client.RecipeSceneExternalClient;
import com.cheftory.api.recipe.content.scene.client.RecipeSceneHttpApi;
import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesRequest;
import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesResponse;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneErrorCode;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneException;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
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

@DisplayName("RecipeSceneExternalClient 테스트")
class RecipeSceneClientTest {

    private MockWebServer mockWebServer;
    private RecipeSceneExternalClient recipeSceneClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        RecipeSceneHttpApi recipeSceneHttpApi = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build()
                .createClient(RecipeSceneHttpApi.class);

        recipeSceneClient = new RecipeSceneExternalClient(recipeSceneHttpApi);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("scene 생성 요청 (fetch)")
    class Fetch {

        @Nested
        @DisplayName("Given - 정상적인 응답이 주어졌을 때")
        class GivenSuccessResponse {
            String fileUri;
            String mimeType;
            UUID stepId;
            List<RecipeStep> recipeSteps;

            @BeforeEach
            void setUp() {
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
                stepId = UUID.randomUUID();

                RecipeStep step = mock(RecipeStep.class);
                doReturn(stepId).when(step).getId();
                doReturn("재료 준비").when(step).getSubtitle();
                doReturn(12.0).when(step).getStart();
                doReturn(List.of(RecipeStep.Detail.of("양파를 썹니다", 12.0), RecipeStep.Detail.of("팬을 달굽니다", 20.0)))
                        .when(step)
                        .getDetails();
                recipeSteps = List.of(step);

                mockWebServer.enqueue(new MockResponse.Builder()
                        .code(HttpStatus.OK.value())
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("""
                                {
                                  "scenes": [
                                    {
                                      "step_id": "%s",
                                      "label": "양파썰기",
                                      "start": 12.0,
                                      "end": 18.0,
                                      "importantScore": 8
                                    }
                                  ]
                                }
                                """.formatted(stepId))
                        .build());
            }

            @Test
            @DisplayName("Then - step_id 기반 scene 응답을 반환하고 요청에도 step_id를 포함한다")
            void thenReturnsScenesAndSendsStepId() throws Exception {
                ClientRecipeScenesResponse result = recipeSceneClient.fetch(fileUri, mimeType, recipeSteps);

                assertThat(result.scenes()).hasSize(1);
                assertThat(result.scenes().getFirst().stepId()).isEqualTo(stepId);
                assertThat(result.scenes().getFirst().label()).isEqualTo("양파썰기");

                RecordedRequest recordedRequest = mockWebServer.takeRequest();
                assertThat(recordedRequest.getMethod()).isEqualTo("POST");
                assertThat(recordedRequest.getTarget()).isEqualTo("/scenes/video");

                ClientRecipeScenesRequest actualRequest =
                        objectMapper.readValue(recordedRequest.getBody().utf8(), ClientRecipeScenesRequest.class);
                assertThat(actualRequest.fileUri()).isEqualTo(fileUri);
                assertThat(actualRequest.mimeType()).isEqualTo(mimeType);
                assertThat(actualRequest.steps()).hasSize(1);
                assertThat(actualRequest.steps().getFirst().stepId()).isEqualTo(stepId);
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

            @Test
            @DisplayName("Then - RecipeSceneException 예외를 던진다")
            void thenThrowsException() {
                Throwable thrown = catchThrowable(() -> recipeSceneClient.fetch(fileUri, mimeType, List.of()));

                assertThat(thrown)
                        .isInstanceOf(RecipeSceneException.class)
                        .hasFieldOrPropertyWithValue("error", RecipeSceneErrorCode.RECIPE_SCENE_CREATE_FAIL);
            }
        }
    }
}
