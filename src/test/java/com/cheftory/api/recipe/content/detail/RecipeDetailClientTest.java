package com.cheftory.api.recipe.content.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipe.content.detail.client.RecipeDetailClient;
import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailRequest;
import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailResponse;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
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

@DisplayName("RecipeDetailClient 테스트")
class RecipeDetailClientTest {

    private MockWebServer mockWebServer;
    private RecipeDetailClient recipeDetailClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        recipeDetailClient = new RecipeDetailClient(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("레시피 상세 정보 조회 (fetch)")
    class Fetch {

        @Nested
        @DisplayName("Given - 유효한 비디오 ID와 파일 정보가 주어졌을 때")
        class GivenValidVideoIdAndFileInfo {

            private String videoId;
            private String fileUri;
            private String mimeType;

            @BeforeEach
            void setUp() {
                videoId = "test-video-123";
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
            }

            @Nested
            @DisplayName("When - 정상 응답이 반환되면")
            class WhenSuccessResponse {

                private ClientRecipeDetailResponse actualResponse;

                @BeforeEach
                void setUp() throws Exception {
                    String responseJson =
                            """
{
	"description": "간단한 요리 설명",
	"cook_time": 30,
	"servings": 4,
	"ingredients": [
		{"name": "재료1", "amount": 2, "unit": "개"},
		{"name": "재료2", "amount": 100, "unit": "g"}
	],
	"tags": ["태그1", "태그2"]
}
""";

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(HttpStatus.OK.value())
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseJson)
                            .build());

                    actualResponse = recipeDetailClient.fetch(videoId, fileUri, mimeType, null);
                }

                @Test
                @DisplayName("Then - 올바른 레시피 상세 정보가 반환된다")
                void thenReturnsCorrectRecipeDetail() {
                    assertThat(actualResponse).isNotNull();
                    assertThat(actualResponse.description()).isEqualTo("간단한 요리 설명");
                    assertThat(actualResponse.cookTime()).isEqualTo(30);
                    assertThat(actualResponse.servings()).isEqualTo(4);
                    assertThat(actualResponse.ingredients()).hasSize(2);
                    assertThat(actualResponse.ingredients().getFirst().name()).isEqualTo("재료1");
                    assertThat(actualResponse.ingredients().getFirst().amount()).isEqualTo(2);
                    assertThat(actualResponse.ingredients().getFirst().unit()).isEqualTo("개");
                    assertThat(actualResponse.tags()).containsExactly("태그1", "태그2");
                }

                @Test
                @DisplayName("Then - 올바른 요청이 전송된다")
                void thenSendsCorrectRequest() throws Exception {
                    RecordedRequest recordedRequest = mockWebServer.takeRequest();
                    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
                    assertThat(recordedRequest.getTarget()).isEqualTo("/meta/video");
                    assertThat(recordedRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE))
                            .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

                    String requestBody = recordedRequest.getBody().utf8();
                    ClientRecipeDetailRequest actualRequest =
                            objectMapper.readValue(requestBody, ClientRecipeDetailRequest.class);

                    assertThat(actualRequest.videoId()).isEqualTo("test-video-123");
                    assertThat(actualRequest.fileUri()).isEqualTo(fileUri);
                    assertThat(actualRequest.mimeType()).isEqualTo(mimeType);
                }
            }

            @Nested
            @DisplayName("When - 서버 에러 응답이 반환되면")
            class WhenServerErrorResponse {

                @BeforeEach
                void setUp() throws Exception {
                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body("{\"error\":\"Invalid request\"}")
                            .build());
                }

                @Test
                @DisplayName("Then - RecipeException이 발생한다")
                void thenThrowsRecipeException() {
                    assertThatThrownBy(() -> recipeDetailClient.fetch(videoId, fileUri, mimeType, null))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_CREATE_FAIL);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 데이터 응답이 주어졌을 때")
        class GivenEmptyDataResponse {

            private String videoId;
            private String fileUri;
            private String mimeType;

            @BeforeEach
            void setUp() {
                videoId = "empty-test";
                fileUri = "s3://bucket/empty.mp4";
                mimeType = "video/mp4";
            }

            @Nested
            @DisplayName("When - 빈 데이터가 포함된 응답이 반환되면")
            class WhenEmptyDataResponse {

                private ClientRecipeDetailResponse actualResponse;

                @BeforeEach
                void setUp() throws Exception {
                    String responseJson =
                            """
{
	"description": "",
	"cook_time": 0,
	"servings": 1,
	"ingredients": [],
	"tags": []
}
""";

                    mockWebServer.enqueue(new MockResponse.Builder()
                            .code(HttpStatus.OK.value())
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(responseJson)
                            .build());

                    actualResponse = recipeDetailClient.fetch(videoId, fileUri, mimeType, null);
                }

                @Test
                @DisplayName("Then - 빈 데이터가 올바르게 처리된다")
                void thenHandlesEmptyDataCorrectly() {
                    assertThat(actualResponse).isNotNull();
                    assertThat(actualResponse.description()).isEmpty();
                    assertThat(actualResponse.cookTime()).isEqualTo(0);
                    assertThat(actualResponse.servings()).isEqualTo(1);
                    assertThat(actualResponse.ingredients()).isEmpty();
                    assertThat(actualResponse.tags()).isEmpty();
                }

                @Test
                @DisplayName("Then - 올바른 요청이 전송된다")
                void thenSendsCorrectRequest() throws Exception {
                    RecordedRequest recordedRequest = mockWebServer.takeRequest();
                    String requestBody = recordedRequest.getBody().utf8();
                    ClientRecipeDetailRequest actualRequest =
                            objectMapper.readValue(requestBody, ClientRecipeDetailRequest.class);

                    assertThat(actualRequest.videoId()).isEqualTo("empty-test");
                    assertThat(actualRequest.fileUri()).isEqualTo(fileUri);
                    assertThat(actualRequest.mimeType()).isEqualTo(mimeType);
                }
            }
        }
    }
}
