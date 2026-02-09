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

    public List<UUID> getRecentSeeds(UUID userId, RankingItemType itemType, int limit) throws CheftoryException {
        String recentKey = rankingInteractionKeyGenerator.recentViewsKey(userId, itemType);
        return rankingInteractionRepository.getLatest(recentKey, limit);
    }
}
