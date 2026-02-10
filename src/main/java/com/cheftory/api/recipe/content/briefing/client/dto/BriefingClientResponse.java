package com.cheftory.api.recipe.content.briefing.client.dto;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 브리핑 생성 외부 API 응답 DTO
 *
 * <p>외부 레시피 브리핑 생성 API로부터 받은 브리핑 정보 응답을 담습니다.</p>
 *
 * @param briefings 브리핑 내용 목록
 */
public record BriefingClientResponse(@JsonProperty("briefings") @NotNull List<String> briefings) {
    /**
     * 응답 DTO를 RecipeBriefing 엔티티 목록으로 변환
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 레시피 브리핑 엔티티 목록
     */
    public List<RecipeBriefing> toRecipeBriefing(UUID recipeId, Clock clock) {
        return briefings.stream()
                .map(briefing -> RecipeBriefing.create(recipeId, briefing, clock))
                .toList();
    }
}
