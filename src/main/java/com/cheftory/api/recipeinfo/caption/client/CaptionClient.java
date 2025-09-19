package com.cheftory.api.recipeinfo.caption.client;

import com.cheftory.api.recipeinfo.caption.client.dto.ClientCaptionErrorResponse;
import com.cheftory.api.recipeinfo.caption.client.dto.ClientCaptionRequest;
import com.cheftory.api.recipeinfo.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipeinfo.caption.client.exception.CaptionClientErrorCode;
import com.cheftory.api.recipeinfo.caption.client.exception.CaptionClientException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class CaptionClient {
  private final WebClient webClient;

  public CaptionClient(@Qualifier("recipeCreateClient") WebClient webClient) {
    this.webClient = webClient;
  }

  public ClientCaptionResponse fetchCaption(String videoId) {
    Objects.requireNonNull(videoId, "videoId는 null일 수 없습니다.");

    log.debug("자막 정보 조회 요청 - videoId: {}", videoId);

    try {
      return webClient
          .post()
          .uri("/captions")
          .bodyValue(ClientCaptionRequest.from(videoId))
          .retrieve()
          .onStatus(
              status -> !status.is2xxSuccessful(),
              response ->
                  response
                      .bodyToMono(ClientCaptionErrorResponse.class)
                      .map(this::mapToException)
                      .onErrorReturn(
                          new CaptionClientException(CaptionClientErrorCode.SERVER_ERROR)))
          .bodyToMono(ClientCaptionResponse.class)
          .block();

    } catch (Exception e) {
      if (e instanceof CaptionClientException) {
        throw e;
      }
      log.error("자막 정보 조회 중 예상치 못한 오류 발생 - videoId: {}", videoId, e);
      throw new CaptionClientException(CaptionClientErrorCode.SERVER_ERROR);
    }
  }

  private CaptionClientException mapToException(ClientCaptionErrorResponse errorResponse) {
    log.warn(
        "API 에러 응답 - Code: {}, Message: {}",
        errorResponse.getErrorCode(),
        errorResponse.getErrorMessage());

    return switch (errorResponse.getErrorCode()) {
      case "CAPTION_001", "CAPTION_004" ->
          new CaptionClientException(CaptionClientErrorCode.NOT_COOK_VIDEO);
      default -> new CaptionClientException(CaptionClientErrorCode.SERVER_ERROR);
    };
  }
}
