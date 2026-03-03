package com.cheftory.api.tracking.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 클릭 엔티티.
 *
 * <p>사용자가 레시피 카드를 클릭(상세 이동)한 기록을 저장합니다.</p>
 */
@Entity
@Table
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class RecipeClick extends MarketScope {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SurfaceType surfaceType;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 레시피 클릭 기록을 생성합니다.
     *
     * @param clock 시간 제공자
     * @param userId 사용자 ID
     * @param requestId 리스트 로드 식별자
     * @param surfaceType 클릭 발생 위치
     * @param recipeId 클릭된 레시피 ID
     * @param position 리스트 내 순서 (0-based)
     * @param frontendTimestamp 프론트엔드 클릭 시각 (Unix ms)
     * @return 레시피 클릭 엔티티
     */
    public static RecipeClick create(
            Clock clock,
            UUID userId,
            UUID requestId,
            SurfaceType surfaceType,
            UUID recipeId,
            int position,
            long frontendTimestamp) {
        return new RecipeClick(
                UUID.randomUUID(),
                userId,
                requestId,
                surfaceType,
                recipeId,
                position,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(frontendTimestamp), ZoneId.of("Asia/Seoul")),
                clock.now());
    }
}
