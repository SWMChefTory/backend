package com.cheftory.api.ranking.snapshot;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 랭킹 스냅샷 서비스.
 *
 * <p>랭킹 생성 시 일관성을 보장하기 위해 PIT(Poing In Time)와 노출 위치를 관리합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class RankingSnapshotService {

    private static final Duration PIT_TTL = Duration.ofMinutes(3);
    private static final Duration POS_TTL = Duration.ofMinutes(30);

    private final RankingSnapshotKeyGenerator keyGenerator;
    private final RankingSnapshotRepository snapshotRepository;

    /**
     * 요청 ID를 발급합니다.
     *
     * @return 요청 ID
     */
    public UUID issueRequestId() {
        return UUID.randomUUID();
    }

    /**
     * PIT를 저장합니다.
     *
     * @param requestId 요청 ID
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     * @param pitId PIT ID
     */
    public void savePit(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType, String pitId) {
        String cacheKey = keyGenerator.pitKey(requestId, surfaceType, itemType);
        snapshotRepository.saveString(cacheKey, pitId, PIT_TTL);
    }

    /**
     * PIT를 조회합니다.
     *
     * @param requestId 요청 ID
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     * @return PIT ID
     */
    public String getPit(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType) {
        String cacheKey = keyGenerator.pitKey(requestId, surfaceType, itemType);
        return snapshotRepository.getString(cacheKey);
    }

    /**
     * PIT와 노출 위치의 만료 시간을 갱신합니다.
     *
     * @param requestId 요청 ID
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     */
    public void refreshPit(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType) {
        snapshotRepository.expire(keyGenerator.pitKey(requestId, surfaceType, itemType), PIT_TTL);
        snapshotRepository.expire(keyGenerator.impressionPosKey(requestId), POS_TTL);
    }

    /**
     * PIT를 삭제합니다.
     *
     * @param requestId 요청 ID
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     */
    public void deletePit(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType) {
        String cacheKey = keyGenerator.pitKey(requestId, surfaceType, itemType);
        snapshotRepository.delete(cacheKey);
    }

    /**
     * 노출 위치를 할당합니다.
     *
     * @param requestId 요청 ID
     * @param count 할당할 개수
     * @return 시작 위치
     * @throws CheftoryException Cheftory 예외
     */
    public long allocateImpressionPositions(UUID requestId, int count) throws CheftoryException {
        String key = keyGenerator.impressionPosKey(requestId);
        Long end = snapshotRepository.incrementLong(key, count);
        snapshotRepository.expire(key, POS_TTL);
        return end - count;
    }

    /**
     * 노출 위치를 초기화합니다.
     *
     * @param requestId 요청 ID
     * @throws CheftoryException Cheftory 예외
     */
    public void clearImpressionPositions(UUID requestId) throws CheftoryException {
        snapshotRepository.delete(keyGenerator.impressionPosKey(requestId));
    }
}
