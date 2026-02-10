package com.cheftory.api.recipe.creation.progress.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 생성 진행 상태 엔티티
 *
 * <p>비동기 레시피 생성 파이프라인의 진행 상태를 추적합니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeProgress extends MarketScope {
    /**
     * 진행 상태 ID
     */
    @Id
    private UUID id;

    /**
     * 생성 일시
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 현재 진행 단계
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipeProgressStep step;

    /**
     * 현재 상세 단계
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecipeProgressDetail detail;

    /**
     * 진행 상태
     */
    @Enumerated(EnumType.STRING)
    @Column
    private RecipeProgressState state;

    /**
     * 연결된 레시피 ID
     */
    @Column(nullable = false)
    private UUID recipeId;

    /**
     * 레시피 진행 상태 생성
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @param step 진행 단계
     * @param detail 상세 단계
     * @param state 진행 상태
     * @return 생성된 레시피 진행 상태 엔티티
     */
    public static RecipeProgress create(
            UUID recipeId,
            Clock clock,
            RecipeProgressStep step,
            RecipeProgressDetail detail,
            RecipeProgressState state) {

        return new RecipeProgress(UUID.randomUUID(), clock.now(), step, detail, state, recipeId);
    }
}
