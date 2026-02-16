package com.cheftory.api.recipe.content.verify.client;

import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientErrorResponse;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientRequest;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.ObjectMapper;

/**
 * 레시피 검증 외부 클라이언트 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeVerifyExternalClient implements RecipeVerifyClient {
    private final RecipeVerifyHttpApi recipeVerifyHttpApi;
    private final ObjectMapper objectMapper;

    /**
     * 레시피 영상 검증 요청
     *
     * <p>외부 API를 호출하여 영상이 요리 관련 콘텐츠인지 확인합니다.</p>
     *
     * @param videoId 유튜브 비디오 ID
     * @return 검증 결과
     * @throws RecipeVerifyException 검증 실패 또는 API 호출 오류 시
     */
    @Override
    public RecipeVerifyClientResponse verify(String videoId) throws RecipeVerifyException {
        Objects.requireNonNull(videoId, "videoId는 null일 수 없습니다.");

        log.debug("영상 검증 요청 - videoId: {}", videoId);

        try {
            return recipeVerifyHttpApi.verify(RecipeVerifyClientRequest.from(videoId));

        } catch (WebClientResponseException e) {
            throw mapToException(e.getResponseBodyAsString());
        } catch (Exception e) {
            if (e instanceof RecipeVerifyException) {
                throw e;
            }
            log.error("영상 검증 중 예상치 못한 오류 발생 - videoId: {}", videoId, e);
            throw new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR, e);
        }
    }

    /**
     * 영상 리소스 정리 요청
     *
     * <p>외부 API를 호출하여 임시 저장된 영상 리소스를 삭제합니다.</p>
     *
     * @param fileUri 파일 URI
     */
    @Override
    public void cleanupVideo(String fileUri) {
        if (fileUri == null || fileUri.isBlank()) return;

        log.debug("영상 리소스 정리 요청 - fileUri: {}", fileUri);

        try {
            recipeVerifyHttpApi.cleanupVideo(fileUri);
        } catch (Exception e) {
            log.warn("영상 리소스 정리 중 오류 발생 - fileUri: {}", fileUri, e);
        }
    }

    private RecipeVerifyException mapToException(String responseBody) {
        try {
            RecipeVerifyClientErrorResponse errorResponse =
                    objectMapper.readValue(responseBody, RecipeVerifyClientErrorResponse.class);
            return mapToException(errorResponse);
        } catch (Exception e) {
            return new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR);
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
