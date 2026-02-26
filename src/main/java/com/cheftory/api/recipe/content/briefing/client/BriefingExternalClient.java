package com.cheftory.api.recipe.content.briefing.client;

import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientRequest;
import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 레시피 브리핑 생성 외부 API 클라이언트 구현체.
 *
 * <p>WebClient를 사용하여 브리핑 생성 API를 호출합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BriefingExternalClient implements BriefingClient {
    private final BriefingHttpApi briefingHttpApi;

    @Override
    public BriefingClientResponse fetchBriefing(String videoId) throws RecipeBriefingException {
        Objects.requireNonNull(videoId, "videoId는 null일 수 없습니다.");

        log.debug("브리핑 생성 요청 - videoId: {}", videoId);

        try {
            return briefingHttpApi.fetchBriefing(BriefingClientRequest.from(videoId));

        } catch (Exception e) {
            log.error("브리핑 생성 중 예상치 못한 오류 발생 - videoId: {}", videoId, e);
            throw new RecipeBriefingException(RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL, e);
        }
    }
}
