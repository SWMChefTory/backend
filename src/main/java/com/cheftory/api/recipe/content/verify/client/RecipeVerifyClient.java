package com.cheftory.api.recipe.content.verify.client;

import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientErrorResponse;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientRequest;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class RecipeVerifyClient {
    private final WebClient webClient;

    public RecipeVerifyClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public RecipeVerifyClientResponse verifyVideo(String videoId) throws RecipeVerifyException {
        Objects.requireNonNull(videoId, "videoId는 null일 수 없습니다.");

        log.debug("영상 검증 요청 - videoId: {}", videoId);

        try {
            return webClient
                    .post()
                    .uri("/verify")
                    .bodyValue(RecipeVerifyClientRequest.from(videoId))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), response -> response.bodyToMono(
                                    RecipeVerifyClientErrorResponse.class)
                            .map(this::mapToException)
                            .onErrorReturn(new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR)))
                    .bodyToMono(RecipeVerifyClientResponse.class)
                    .block();

        } catch (Exception e) {
            if (e instanceof RecipeVerifyException) {
                throw e;
            }
            log.error("영상 검증 중 예상치 못한 오류 발생 - videoId: {}", videoId, e);
            throw new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR);
        }
    }

    public void cleanupVideo(String fileUri) {
        if (fileUri == null || fileUri.isBlank()) return;

        log.debug("영상 리소스 정리 요청 - fileUri: {}", fileUri);

        try {
            webClient
                    .delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/cleanup")
                            .queryParam("file_uri", fileUri)
                            .build())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.warn("영상 리소스 정리 중 오류 발생 - fileUri: {}", fileUri, e);
        }
    }

    private RecipeVerifyException mapToException(RecipeVerifyClientErrorResponse errorResponse) {
        log.warn("API 에러 응답 - Code: {}, Message: {}", errorResponse.getErrorCode(), errorResponse.getErrorMessage());

        return switch (errorResponse.getErrorCode()) {
            case "VERIFY_003" -> new RecipeVerifyException(RecipeVerifyErrorCode.NOT_COOK_VIDEO);
            default -> new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR);
        };
    }
}
