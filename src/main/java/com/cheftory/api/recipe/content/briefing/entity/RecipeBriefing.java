package com.cheftory.api.recipe.content.briefing.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 브리핑 엔티티
 *
 * <p>레시피에 대한 요약 정보나 특징을 저장하는 엔티티입니다.</p>
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeBriefing extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 레시피 브리핑 생성
     *
     * @param recipeId 레시피 ID
     * @param content 브리핑 내용
     * @param clock 현재 시간 제공 객체
     * @return 생성된 레시피 브리핑 엔티티
     */
    public static RecipeBriefing create(UUID recipeId, String content, Clock clock) {
        return new RecipeBriefing(UUID.randomUUID(), recipeId, content, clock.now());
    }
}
