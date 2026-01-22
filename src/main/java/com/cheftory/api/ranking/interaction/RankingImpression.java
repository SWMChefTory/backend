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

@Entity
@Table
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class RankingImpression extends MarketScope {

    @Id
    private UUID id;

    @Column
    private UUID requestId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RankingItemType itemType;

    @Column(nullable = false)
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RankingSurfaceType surfaceType;

    @Column(nullable = false)
    private Long position;

    @Column(nullable = false)
    private LocalDateTime createdAt;

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
