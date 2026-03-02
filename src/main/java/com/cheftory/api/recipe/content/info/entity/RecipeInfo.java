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
 * <p>레시피의 상태, 조회수, 생성일시와 함께 소스 식별자(`sourceType/sourceKey`) 및
 * 현재 비동기 생성 실행 식별자(`currentJobId`)를 관리합니다.</p>
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "recipe",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_recipe_market_source_type_source_key",
                    columnNames = {"market", "source_type", "source_key"})
        })
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

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RecipeSourceType sourceType;

    @Column(length = 128, nullable = false)
    private String sourceKey;

    @Column(nullable = false)
    private long creditCost;

    @Column(nullable = false)
    private UUID currentJobId;

    @Column(nullable = false)
    private boolean isPublic = false;

    /**
     * 신규 레시피를 생성합니다.
     *
     * <p>초기 상태는 `IN_PROGRESS`이며, 중복 방지/진행 추적을 위해 `sourceType/sourceKey`와 `currentJobId`를 함께 설정합니다.</p>
     */
    public static RecipeInfo create(Clock clock, RecipeSourceType sourceType, String sourceKey) {
        LocalDateTime now = clock.now();
        return new RecipeInfo(
                UUID.randomUUID(),
                INITIAL_VIEW_COUNT,
                now,
                now,
                RecipeStatus.IN_PROGRESS,
                sourceType,
                sourceKey,
                DEFAULT_CREDIT_COST,
                UUID.randomUUID(),
						false);
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

    public void banned(Clock clock) {
        this.updatedAt = clock.now();
        this.recipeStatus = RecipeStatus.BANNED;
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

    public boolean isBanned() {
        return this.recipeStatus == RecipeStatus.BANNED;
    }
}
