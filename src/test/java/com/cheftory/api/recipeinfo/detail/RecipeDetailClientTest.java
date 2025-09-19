package com.cheftory.api.recipeinfo.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.detail.client.RecipeDetailClient;
import com.cheftory.api.recipeinfo.detail.client.dto.ClientRecipeDetailRequest;
import com.cheftory.api.recipeinfo.detail.client.dto.ClientRecipeDetailResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@DisplayName("RecipeDetailClient 테스트")
class RecipeDetailClientTest {

  private MockWebServer mockWebServer;
  private RecipeDetailClient recipeDetailClient;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws IOException {
    // MockWebServer 설정
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    // WebClient 설정
    WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

    recipeDetailClient = new RecipeDetailClient(webClient);
    objectMapper = new ObjectMapper();
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Nested
  @DisplayName("레시피 상세 정보 조회")
  class FetchRecipeDetails {

    @Nested
    @DisplayName("Given - 유효한 비디오 ID와 캡션이 주어졌을 때")
    class GivenValidVideoIdAndCaption {

      private String videoId;
      private RecipeCaption recipeCaption;

      @BeforeEach
      void setUp() {
        videoId = "test-video-123";
        recipeCaption = mock(RecipeCaption.class);

        // Mock RecipeCaption 설정
        RecipeCaption.Segment segment1 = mock(RecipeCaption.Segment.class);
        doReturn(0.0).when(segment1).getStart();
        doReturn(10.0).when(segment1).getEnd();
        doReturn("첫 번째 캡션").when(segment1).getText();

        RecipeCaption.Segment segment2 = mock(RecipeCaption.Segment.class);
        doReturn(10.0).when(segment2).getStart();
        doReturn(20.0).when(segment2).getEnd();
        doReturn("두 번째 캡션").when(segment2).getText();

        doReturn(List.of(segment1, segment2)).when(recipeCaption).getSegments();
      }

      @Test
      @DisplayName("정상 응답 시 올바른 레시피 상세 정보가 반환된다")
      void shouldReturnCorrectRecipeDetailOnSuccess() throws Exception {
        // Given
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
        mockWebServer.enqueue(
            new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        // When
        ClientRecipeDetailResponse actualResponse =
            recipeDetailClient.fetchRecipeDetails(videoId, recipeCaption);

        // Then - 응답 DTO 검증
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.description()).isEqualTo("간단한 요리 설명");
        assertThat(actualResponse.cookTime()).isEqualTo(30);
        assertThat(actualResponse.servings()).isEqualTo(4);
        assertThat(actualResponse.ingredients()).hasSize(2);
        assertThat(actualResponse.ingredients().get(0).name()).isEqualTo("재료1");
        assertThat(actualResponse.ingredients().get(0).amount()).isEqualTo(2);
        assertThat(actualResponse.ingredients().get(0).unit()).isEqualTo("개");
        assertThat(actualResponse.tags()).containsExactly("태그1", "태그2");

        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/meta");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE))
            .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        // 요청 DTO 검증
        String requestBody = recordedRequest.getBody().readUtf8();
        ClientRecipeDetailRequest actualRequest =
            objectMapper.readValue(requestBody, ClientRecipeDetailRequest.class);
        assertThat(actualRequest.videoId()).isEqualTo("test-video-123");
        assertThat(actualRequest.captions()).hasSize(2);
        // Caption의 내부 필드는 private이므로 JSON 문자열로 검증
        assertThat(requestBody).contains("첫 번째 캡션");
        assertThat(requestBody).contains("두 번째 캡션");
      }

      @Test
      @DisplayName("서버 에러 시 WebClientResponseException이 발생한다")
      void shouldThrowWebClientResponseExceptionOnServerError() throws InterruptedException {
        // Given
        mockWebServer.enqueue(
            new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("{\"error\":\"Invalid request\"}"));

        // When & Then
        assertThatThrownBy(() -> recipeDetailClient.fetchRecipeDetails(videoId, recipeCaption))
            .isInstanceOf(WebClientResponseException.class)
            .satisfies(
                exception -> {
                  WebClientResponseException webClientException =
                      (WebClientResponseException) exception;
                  assertThat(webClientException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/meta");
      }
    }

    @Nested
    @DisplayName("Given - DTO 검증 특수 케이스")
    class GivenDtoValidationEdgeCases {

      @Test
      @DisplayName("빈 데이터가 포함된 DTO가 올바르게 처리된다")
      void shouldHandleEmptyDataCorrectly() throws Exception {
        // Given
        String videoId = "empty-test";
        RecipeCaption recipeCaption = mock(RecipeCaption.class);
        doReturn(List.of()).when(recipeCaption).getSegments();

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
        mockWebServer.enqueue(
            new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        // When
        ClientRecipeDetailResponse actualResponse =
            recipeDetailClient.fetchRecipeDetails(videoId, recipeCaption);

        // Then - 응답 DTO 빈 값 검증
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.description()).isEmpty();
        assertThat(actualResponse.cookTime()).isEqualTo(0);
        assertThat(actualResponse.servings()).isEqualTo(1);
        assertThat(actualResponse.ingredients()).isEmpty();
        assertThat(actualResponse.tags()).isEmpty();

        // 요청 DTO 빈 값 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String requestBody = recordedRequest.getBody().readUtf8();
        ClientRecipeDetailRequest actualRequest =
            objectMapper.readValue(requestBody, ClientRecipeDetailRequest.class);
        assertThat(actualRequest.videoId()).isEqualTo("empty-test");
        assertThat(actualRequest.captions()).isEmpty();
      }
    }
  }
}
