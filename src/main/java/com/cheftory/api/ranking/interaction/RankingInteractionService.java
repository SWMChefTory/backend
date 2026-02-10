package com.cheftory.api.ranking.interaction;

import com.cheftory.api._common.Clock;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 랭킹 상호작용 서비스.
 *
 * <p>사용자의 랭킹 노출 및 이벤트(클릭, 조회 등)를 추적하고 관리합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingInteractionService {

    private static final Duration RECENT_VIEWS_WINDOW = Duration.ofDays(30);
    private static final Duration RECENT_VIEWS_TTL = Duration.ofDays(60);

    private final RankingEventRepository rankingEventRepository;
    private final RankingImpressionRepository rankingImpressionRepository;
    private final RankingInteractionKeyGenerator rankingInteractionKeyGenerator;
    private final RankingInteractionRepository rankingInteractionRepository;
    private final Clock clock;

    /**
     * 랭킹 노출을 기록합니다.
     *
     * @param userId 사용자 ID
     * @param surfaceType 노출 서피스 타입
     * @param itemType 아이템 타입
     * @param itemIds 아이템 ID 목록
     * @param requestId 요청 ID
     * @param positionStart 시작 위치
     */
    @Transactional
    public void logImpressions(
            UUID userId,
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            List<UUID> itemIds,
            UUID requestId,
            long positionStart) {

        List<RankingImpression> impressions = IntStream.range(0, itemIds.size())
                .mapToObj(i -> RankingImpression.create(
                        requestId, userId, itemType, itemIds.get(i), surfaceType, positionStart + i, clock))
                .toList();

        rankingImpressionRepository.saveAll(impressions);
    }

    /**
     * 랭킹 이벤트를 기록합니다.
     *
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @param itemId 아이템 ID
     * @param eventType 이벤트 타입
     * @param requestId 요청 ID
     * @throws CheftoryException Cheftory 예외
     */
    public void logEvent(UUID userId, RankingItemType itemType, UUID itemId, RankingEventType eventType, UUID requestId)
            throws CheftoryException {
        rankingEventRepository.save(RankingEvent.create(userId, itemType, itemId, eventType, requestId, clock));

        if (eventType == RankingEventType.VIEW) {
            long now = clock.nowMillis();
            String recentKey = rankingInteractionKeyGenerator.recentViewsKey(userId, itemType);

            rankingInteractionRepository.add(recentKey, itemId, now);

            long cutoff = now - RECENT_VIEWS_WINDOW.toMillis();
            rankingInteractionRepository.pruneBefore(recentKey, cutoff);

            rankingInteractionRepository.expire(recentKey, RECENT_VIEWS_TTL);
        }
    }

    /**
     * 최근 조회 시드 아이템을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @param limit 최대 개수
     * @return 시드 아이템 ID 목록
     * @throws CheftoryException Cheftory 예외
     */
    public List<UUID> getRecentSeeds(UUID userId, RankingItemType itemType, int limit) throws CheftoryException {
        String recentKey = rankingInteractionKeyGenerator.recentViewsKey(userId, itemType);
        return rankingInteractionRepository.getLatest(recentKey, limit);
    }
}
