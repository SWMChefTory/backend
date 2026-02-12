package com.cheftory.api.recipe.content.briefing.client;

import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;

/**
 * 레시피 브리핑 생성 클라이언트 인터페이스.
 *
 * <p>외부 API를 호출하여 레시피 브리핑을 생성합니다.</p>
 */
public interface BriefingClient {
    /**
     * 비디오 ID로 브리핑을 생성합니다.
     *
     * @param videoId YouTube 비디오 ID
     * @return 생성된 브리핑 응답
     * @throws RecipeBriefingException 브리핑 생성 실패 시
     */
    BriefingClientResponse fetchBriefing(String videoId) throws RecipeBriefingException;
}
