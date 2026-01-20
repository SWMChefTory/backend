package com.cheftory.api.recipe.content.briefing.client;

import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientRequest;
import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.client.exception.BriefingClientErrorCode;
import com.cheftory.api.recipe.content.briefing.client.exception.BriefingClientException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class BriefingClient {
    private final WebClient webClient;

    public BriefingClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public BriefingClientResponse fetchBriefing(String videoId) {
        Objects.requireNonNull(videoId, "videoId는 null일 수 없습니다.");

        log.debug("브리핑 생성 요청 - videoId: {}", videoId);

        try {
            return webClient
                    .post()
                    .uri("/briefings")
                    .bodyValue(BriefingClientRequest.from(videoId))
                    .retrieve()
                    .bodyToMono(BriefingClientResponse.class)
                    .block();

        } catch (Exception e) {
            if (e instanceof BriefingClientException) {
                throw e;
            }
            log.error("브리핑 생성 중 예상치 못한 오류 발생 - videoId: {}", videoId, e);
            throw new BriefingClientException(BriefingClientErrorCode.SERVER_ERROR);
        }
    }
}
