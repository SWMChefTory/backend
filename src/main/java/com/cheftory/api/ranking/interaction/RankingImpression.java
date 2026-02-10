package com.cheftory.api.ranking.interaction;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 랭킹 노출(impression) 엔티티.
 *
 * <p>사용자에게 랭킹 아이템이 노출된 기록을 저장합니다.</p>
 */
@Entity
@Table
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class RankingImpression extends MarketScope {

    /** 노출 ID */
    @Id
    private UUID id;

    /** 요청 ID */
    @Column
    private UUID requestId;

    /** 사용자 ID */
    @Column(nullable = false)
    private UUID userId;

    /** 아이템 타입 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RankingItemType itemType;

    /** 아이템 ID */
    @Column(nullable = false)
    private UUID itemId;

    /** 서피스 타입 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RankingSurfaceType surfaceType;

    /** 노출 위치 */
    @Column(nullable = false)
    private Long position;

    /** 생성 시각 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 랭킹 노출 기록을 생성합니다.
     *
     * @param requestId 요청 ID
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @param itemId 아이템 ID
     * @param surfaceType 서피스 타입
     * @param position 노출 위치
     * @param clock 시간 제공자
     * @return 랭킹 노출
     */
    public static RankingImpression create(
            UUID requestId,
            UUID userId,
            RankingItemType itemType,
            UUID itemId,
            RankingSurfaceType surfaceType,
            long position,
            Clock clock) {

        return new RankingImpression(
                UUID.randomUUID(), requestId, userId, itemType, itemId, surfaceType, position, clock.now());
    }
}
