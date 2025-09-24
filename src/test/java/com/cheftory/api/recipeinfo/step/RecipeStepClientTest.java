package com.cheftory.api.recipeinfo.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.step.client.RecipeStepClient;
import com.cheftory.api.recipeinfo.step.client.dto.ClientRecipeStepsRequest;
import com.cheftory.api.recipeinfo.step.client.dto.ClientRecipeStepsResponse;
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

@DisplayName("RecipeStepClient 테스트")
class RecipeStepClientTest {

  private MockWebServer mockWebServer;
  private RecipeStepClient recipeStepClient;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws IOException {
    // MockWebServer 설정
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    // WebClient 설정
    WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

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

    @Nested
    @DisplayName("Given - 유효한 캡션이 주어졌을 때")
    class GivenValidCaption {

      private RecipeCaption recipeCaption;
      private List<RecipeCaption.Segment> segments;

      @BeforeEach
      void setUp() {
        recipeCaption = mock(RecipeCaption.class);

        // RecipeCaption.Segment mock 설정
        RecipeCaption.Segment segment1 = mock(RecipeCaption.Segment.class);
        doReturn(0.0).when(segment1).getStart();
        doReturn(10.0).when(segment1).getEnd();
        doReturn("첫 번째 캡션 텍스트").when(segment1).getText();

        RecipeCaption.Segment segment2 = mock(RecipeCaption.Segment.class);
        doReturn(10.0).when(segment2).getStart();
        doReturn(20.0).when(segment2).getEnd();
        doReturn("두 번째 캡션 텍스트").when(segment2).getText();

        segments = List.of(segment1, segment2);

        // Mock RecipeCaption 설정
        doReturn(segments).when(recipeCaption).getSegments();
      }

      @Test
      @DisplayName("정상 응답 시 올바른 레시피 단계 목록이 반환된다")
      void shouldReturnCorrectRecipeStepsOnSuccess() throws Exception {
        // Given
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
        mockWebServer.enqueue(
            new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        // When
        ClientRecipeStepsResponse actualResponse = recipeStepClient.fetchRecipeSteps(recipeCaption);

        // Then - 응답 DTO 검증
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.steps()).hasSize(2);

        // 첫 번째 단계 상세 검증
        ClientRecipeStepsResponse.Step firstStep = actualResponse.steps().get(0);
        assertThat(firstStep.subtitle()).isEqualTo("첫 번째 단계");
        assertThat(firstStep.start()).isEqualTo(10.0);
        assertThat(firstStep.descriptions()).hasSize(2);
        // Description 리스트 검증 (가시성 문제로 인해 리스트 수와 구조만 검증)
        assertThat(firstStep.descriptions()).isNotEmpty();

        // 두 번째 단계 상세 검증
        ClientRecipeStepsResponse.Step secondStep = actualResponse.steps().get(1);
        assertThat(secondStep.subtitle()).isEqualTo("두 번째 단계");
        assertThat(secondStep.start()).isEqualTo(30.0);
        assertThat(secondStep.descriptions()).hasSize(1);
        assertThat(secondStep.descriptions()).isNotEmpty();

        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/steps");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE))
            .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        // 요청 DTO 검증
        String requestBody = recordedRequest.getBody().readUtf8();
        ClientRecipeStepsRequest actualRequest =
            objectMapper.readValue(requestBody, ClientRecipeStepsRequest.class);
        assertThat(actualRequest.recipeCaptions()).hasSize(2);

        // 첫 번째 캡션 검증
        ClientRecipeStepsRequest.Caption firstCaption = actualRequest.recipeCaptions().get(0);
        assertThat(firstCaption.start()).isEqualTo(0.0);
        assertThat(firstCaption.end()).isEqualTo(10.0);
        assertThat(firstCaption.text()).isEqualTo("첫 번째 캡션 텍스트");

        // 두 번째 캡션 검증
        ClientRecipeStepsRequest.Caption secondCaption = actualRequest.recipeCaptions().get(1);
        assertThat(secondCaption.start()).isEqualTo(10.0);
        assertThat(secondCaption.end()).isEqualTo(20.0);
        assertThat(secondCaption.text()).isEqualTo("두 번째 캡션 텍스트");
      }

      @Test
      @DisplayName("서버 에러 시 WebClientResponseException이 발생한다")
      void shouldThrowWebClientResponseExceptionOnServerError() throws InterruptedException {
        // Given
        mockWebServer.enqueue(
            new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("{\"error\":\"Invalid caption data\"}"));

        // When & Then
        assertThatThrownBy(() -> recipeStepClient.fetchRecipeSteps(recipeCaption))
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
        assertThat(recordedRequest.getPath()).isEqualTo("/steps");
      }
    }

    @Nested
    @DisplayName("Given - 빈 캡션 세그먼트가 주어졌을 때")
    class GivenEmptyCaptionSegments {

      private RecipeCaption recipeCaption;

      @BeforeEach
      void setUp() {
        recipeCaption = mock(RecipeCaption.class);
        doReturn(List.of()).when(recipeCaption).getSegments();

        mockWebServer.enqueue(
            new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"steps\": []}"));
      }

      @Test
      @DisplayName("빈 단계 목록이 반환된다")
      void shouldReturnEmptyStepsWhenEmptySegments() throws Exception {
        // When
        ClientRecipeStepsResponse actualResponse = recipeStepClient.fetchRecipeSteps(recipeCaption);

        // Then - 응답 DTO 검증
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.steps()).isEmpty();

        // 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/steps");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE))
            .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        // 요청 DTO 검증 - 빈 캡션 리스트가 전송되었는지 확인
        String requestBody = recordedRequest.getBody().readUtf8();
        ClientRecipeStepsRequest actualRequest =
            objectMapper.readValue(requestBody, ClientRecipeStepsRequest.class);
        assertThat(actualRequest.recipeCaptions()).isEmpty();
      }
    }

    @Nested
    @DisplayName("Given - DTO 검증 특수 케이스")
    class GivenDtoValidationEdgeCases {

      @Test
      @DisplayName("숫자값과 특수문자가 포함된 DTO가 올바르게 처리된다")
      void shouldHandleDtoWithSpecialValues() throws Exception {
        // Given
        RecipeCaption recipeCaption = mock(RecipeCaption.class);

        RecipeCaption.Segment segment = mock(RecipeCaption.Segment.class);
        doReturn(0.123).when(segment).getStart();
        doReturn(15.789).when(segment).getEnd();
        doReturn("특수문자 & 숫자 123: \"따옴표\" 테스트").when(segment).getText();

        doReturn(List.of(segment)).when(recipeCaption).getSegments();

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
        mockWebServer.enqueue(
            new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        // When
        ClientRecipeStepsResponse actualResponse = recipeStepClient.fetchRecipeSteps(recipeCaption);

        // Then - 응답 DTO 특수값 검증
        assertThat(actualResponse.steps()).hasSize(1);
        ClientRecipeStepsResponse.Step step = actualResponse.steps().get(0);
        assertThat(step.subtitle()).isEqualTo("특수 단계 & 테스트");
        assertThat(step.start()).isEqualTo(0.123);
        assertThat(step.descriptions()).hasSize(1);
        assertThat(step.descriptions()).isNotEmpty();

        // 요청 DTO 특수값 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String requestBody = recordedRequest.getBody().readUtf8();
        ClientRecipeStepsRequest actualRequest =
            objectMapper.readValue(requestBody, ClientRecipeStepsRequest.class);

        assertThat(actualRequest.recipeCaptions()).hasSize(1);
        ClientRecipeStepsRequest.Caption caption = actualRequest.recipeCaptions().get(0);
        assertThat(caption.start()).isEqualTo(0.123);
        assertThat(caption.end()).isEqualTo(15.789);
        assertThat(caption.text()).isEqualTo("특수문자 & 숫자 123: \"따옴표\" 테스트");
      }
    }
  }
}
