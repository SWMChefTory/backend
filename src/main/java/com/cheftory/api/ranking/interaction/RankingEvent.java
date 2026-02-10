package com.cheftory.api.ranking.interaction;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
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
 * 랭킹 이벤트 엔티티.
 *
 * <p>사용자의 랭킹 관련 이벤트(클릭, 조회 등)를 저장합니다.</p>
 */
@Entity
@Table
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class RankingEvent extends MarketScope {

    /** 이벤트 ID */
    @Id
    private UUID id;

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

    /** 이벤트 타입 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RankingEventType eventType;

    /** 요청 ID */
    @Column
    private UUID requestId;

    /** 생성 시각 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 랭킹 이벤트를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @param itemId 아이템 ID
     * @param eventType 이벤트 타입
     * @param requestId 요청 ID
     * @param clock 시간 제공자
     * @return 랭킹 이벤트
     */
    public static RankingEvent create(
            UUID userId,
            RankingItemType itemType,
            UUID itemId,
            RankingEventType eventType,
            UUID requestId,
            Clock clock) {
        return new RankingEvent(UUID.randomUUID(), userId, itemType, itemId, eventType, requestId, clock.now());
    }
}
