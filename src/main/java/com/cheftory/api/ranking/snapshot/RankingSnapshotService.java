package com.cheftory.api.ranking.snapshot;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingSnapshotService {

    private static final Duration PIT_TTL = Duration.ofMinutes(3);
    private static final Duration POS_TTL = Duration.ofMinutes(30);

    private final RankingSnapshotKeyGenerator keyGenerator;
    private final RankingSnapshotRepository snapshotRepository;

    public UUID issueRequestId() {
        return UUID.randomUUID();
    }

    public void savePit(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType, String pitId) {
        String cacheKey = keyGenerator.pitKey(requestId, surfaceType, itemType);
        snapshotRepository.saveString(cacheKey, pitId, PIT_TTL);
    }

    public String getPit(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType) {
        String cacheKey = keyGenerator.pitKey(requestId, surfaceType, itemType);
        return snapshotRepository.getString(cacheKey);
    }

    public void refreshPit(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType) {
        snapshotRepository.expire(keyGenerator.pitKey(requestId, surfaceType, itemType), PIT_TTL);
        snapshotRepository.expire(keyGenerator.impressionPosKey(requestId), POS_TTL);
    }

    public void deletePit(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType) {
        String cacheKey = keyGenerator.pitKey(requestId, surfaceType, itemType);
        snapshotRepository.delete(cacheKey);
    }

    public long allocateImpressionPositions(UUID requestId, int count) {
        String key = keyGenerator.impressionPosKey(requestId);
        Long end = snapshotRepository.incrementLong(key, count);
        snapshotRepository.expire(key, PIT_TTL);
        return end - count;
    }

    public void clearImpressionPositions(UUID requestId) {
        snapshotRepository.delete(keyGenerator.impressionPosKey(requestId));
    }
}
