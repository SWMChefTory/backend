package com.cheftory.api.recipe.caption.client;

import com.cheftory.api.exception.ErrorMessage;
import com.cheftory.api.exception.ExternalServerNetworkException;
import com.cheftory.api.exception.ExternalServerNetworkExceptionCode;
import com.cheftory.api.recipe.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipe.caption.client.exception.CaptionClientErrorCode;
import com.cheftory.api.recipe.caption.client.exception.CaptionClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptionClientTest {

  private CaptionClient captionClient;
  @Mock
  private WebClient webClient;
  @Mock
  private ExchangeFunction mockExchange;

  @BeforeEach
  void setUp() {
    WebClient.builder().exchangeFunction(mockExchange).build();
    captionClient = new CaptionClient(webClient);
  }

  @Nested
  @DisplayName("Given : 유효한 id가 들어올 때")
  class GivenValidVideoId {

    private String jsonResponse;
    private ClientCaptionResponse response;
    private String videoId;

    @BeforeEach
    void setUp() throws Exception {
      InputStream is = getClass()
          .getClassLoader()
          .getResourceAsStream("captions/caption_client_response.json");

      jsonResponse = new String(is.readAllBytes(), StandardCharsets.UTF_8);

      ObjectMapper mapper = new ObjectMapper();
      response = mapper.readValue(jsonResponse, ClientCaptionResponse.class);
      videoId = "videoId123";
    }

    @Nested
    @DisplayName("When : 자막에 대해 생성한다면")
    class WhenFetchCaption {

      @BeforeEach
      void setUp() {
        ClientResponse mockResponse = ClientResponse.create(org.springframework.http.HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body(jsonResponse)
            .build();

        when(mockExchange.exchange(any(ClientRequest.class)))
            .thenReturn(Mono.just(mockResponse));
      }

      @Test
      @DisplayName("Then : 성공적으로 자막 정보를 받아온다.")
      void then() {
        ClientCaptionResponse resp = captionClient.fetchCaption(videoId);
        assertNotNull(resp);
      }
    }
  }


  @Nested
  @DisplayName("Given : id가 null일 때")
  class GivenNullId {

    @Nested
    @DisplayName("When : 자막에 대해 생성한다면")
    class WhenFetchCaption {

      @Test
      @DisplayName("Then : 예외가 발생한다.")
      void then() {
        Throwable exception = assertThrows(NullPointerException.class,
            () -> captionClient.fetchCaption(null));
        assertEquals("videoId는 null이 될 수 없습니다.", exception.getMessage());
      }
    }
  }


  @Nested
  @DisplayName("Given : videoId가 주어질 때")
  class GivenVideoId {

    String videoId;

    @Nested
    @DisplayName("When : 외부 API네트워크 장애가 발생한다면")
    class WhenNetworkFails {

      @BeforeEach
      void setUp() {
        when(mockExchange.exchange(any(ClientRequest.class)))
            .thenThrow(new WebClientRequestException(
                new IOException("Connection timed out"),
                HttpMethod.POST,
                URI.create("http://fake-uri.com"),
                HttpHeaders.EMPTY
            ));
      }

      @Test
      @DisplayName("Then : ExternalServerException 예외가 던져진다.")
      void then_throwsCaptionClientException() {
        ExternalServerNetworkException exception = assertThrows(
            ExternalServerNetworkException.class,
            () -> captionClient.fetchCaption(videoId)
        );
        assertEquals(ExternalServerNetworkExceptionCode.UNKNOWN_SERVER_ERROR,
            exception.getErrorCode());
      }
    }

    @Nested
    @DisplayName("When : 외부 API에서 400과 함께 NOT_CAPTION_VIDEO을 보낸다면")
    class WhenServerReturns400NotCaptionUrl {

      @BeforeEach
      void setUp() {
        String errorJson = """
              {
                "errorCode": "NOT_CAPTION_VIDEO",
                "message": "요리 비디오가 아닙니다."
              }
            """;

        ClientResponse errorResponse = ClientResponse.create(
                org.springframework.http.HttpStatus.BAD_REQUEST)
            .header("Content-Type", "application/json")
            .body(errorJson)
            .build();

        when(mockExchange.exchange(any(ClientRequest.class)))
            .thenReturn(Mono.just(errorResponse));
      }

      @Test
      @DisplayName("Then : CaptionClientException(NOT_CAPTION_VIDEO)을 던진다.")
      void thenThrowsCaptionClientException_003() {
        CaptionClientException exception = assertThrows(
            CaptionClientException.class,
            () -> captionClient.fetchCaption(videoId)
        );

        ErrorMessage error = exception.getErrorMessage();
        assertEquals(CaptionClientErrorCode.NOT_COOK_VIDEO, error.getErrorCode());

      }
    }

    @Nested
    @DisplayName("When : 외부 API에서 500 에러를 반환한다면")
    class WhenServerReturns500 {

      @BeforeEach
      void setUp() {
        ClientResponse errorResponse = ClientResponse.create(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Content-Type", "application/json")
            .body("""
                  {
                    "errorCode": "INTERNAL_ERROR",
                    "message": "서버 에러 발생"
                  }
                """)
            .build();

        when(mockExchange.exchange(any(ClientRequest.class)))
            .thenReturn(Mono.just(errorResponse));
      }

      @Test
      @DisplayName("Then : CaptionClientException 예외를 던진다.")
      void thenThrowsCaptionClientException_500() {
        CaptionClientException exception = assertThrows(
            CaptionClientException.class,
            () -> captionClient.fetchCaption(videoId)
        );

        ErrorMessage error = exception.getErrorMessage();
        assertEquals(CaptionClientErrorCode.SERVER_ERROR, error);
      }
    }
  }

}