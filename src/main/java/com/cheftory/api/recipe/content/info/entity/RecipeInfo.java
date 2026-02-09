package com.cheftory.api.recipe.content.info.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

/**
 * 레시피 기본 정보 엔티티
 *
 * <p>레시피의 상태, 조회수, 생성일시 등 핵심적인 메타데이터를 관리하는 엔티티입니다.</p>
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "recipe")
public class RecipeInfo extends MarketScope {

    private static final int INITIAL_VIEW_COUNT = 0;
    private static final long DEFAULT_CREDIT_COST = 1L;

    @Id
    private UUID id;

    @Column(nullable = false)
    private Integer viewCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private RecipeStatus recipeStatus;

    @Column(nullable = false)
    private long creditCost;

    /**
     * 레시피 기본 정보 생성
     *
     * @param clock 현재 시간 제공 객체
     * @return 생성된 레시피 정보 엔티티 (진행 중 상태)
     */
    public static RecipeInfo create(Clock clock) {
        LocalDateTime now = clock.now();
        return new RecipeInfo(
                UUID.randomUUID(), INITIAL_VIEW_COUNT, now, now, RecipeStatus.IN_PROGRESS, DEFAULT_CREDIT_COST);
    }

    /**
     * 레시피 상태를 성공으로 변경
     *
     * @param clock 현재 시간 제공 객체
     */
    public void success(Clock clock) {
        this.updatedAt = clock.now();
        this.recipeStatus = RecipeStatus.SUCCESS;
    }

    /**
     * 레시피 상태를 실패로 변경
     *
     * @param clock 현재 시간 제공 객체
     */
    public void failed(Clock clock) {
        this.updatedAt = clock.now();
        this.recipeStatus = RecipeStatus.FAILED;
    }

    /**
     * 레시피 차단 처리
     *
     * @param clock 현재 시간 제공 객체
     */
    public void block(Clock clock) {
        this.updatedAt = clock.now();
        this.recipeStatus = RecipeStatus.BLOCKED;
    }

    /**
     * 성공 상태 여부 확인
     *
     * @return 성공 상태이면 true
     */
    public boolean isSuccess() {
        return this.recipeStatus == RecipeStatus.SUCCESS;
    }

    /**
     * 실패 상태 여부 확인
     *
     * @return 실패 상태이면 true
     */
    public boolean isFailed() {
        return this.recipeStatus == RecipeStatus.FAILED;
    }

    /**
     * 차단 상태 여부 확인
     *
     * @return 차단 상태이면 true
     */
    public boolean isBlocked() {
        return this.recipeStatus == RecipeStatus.BLOCKED;
    }
}
