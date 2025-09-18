package com.cheftory.api.recipe.caption.client;

import com.cheftory.api.recipe.caption.client.dto.ClientCaptionRequest;
import com.cheftory.api.recipe.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipe.caption.client.exception.CaptionClientException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CaptionClient {
    public CaptionClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    private final WebClient webClient;

    /**
     * 자막 정보를 외부 API에서 조회합니다.
     *
     * @param videoId 유튜브 비디오 ID
     * @return 자막 정보
     * @throws CaptionClientException API 오류 또는 응답 실패 시 발생
     * @throws NullPointerException videoId가 null인 경우
     */
    public ClientCaptionResponse fetchCaption(String videoId) {
        ClientCaptionRequest request = ClientCaptionRequest
                .from(videoId);
        return webClient.post().uri(uriBuilder -> uriBuilder
                        .path("/captions")
                        .build()
                ).bodyValue(request)
                .retrieve()
                .bodyToMono(ClientCaptionResponse.class)
                .block();
    }
}
