package com.cheftory.api.ranking.interaction;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 랭킹 상호작용 Redis 리포지토리.
 *
 * <p>Redis Sorted Set을 사용하여 최근 조회 이력을 관리합니다.</p>
 */
@Repository
@RequiredArgsConstructor
public class RankingInteractionRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 아이템을 추가합니다.
     *
     * @param key 키
     * @param itemId 아이템 ID
     * @param epochMillis 에포크 밀리초 (score)
     */
    public void add(String key, UUID itemId, long epochMillis) {
        redisTemplate.opsForZSet().add(key, itemId.toString(), (double) epochMillis);
    }

    /**
     * 최근 아이템을 조회합니다.
     *
     * @param key 키
     * @param limit 최대 개수
     * @return 아이템 ID 목록
     */
    public List<UUID> getLatest(String key, long limit) {
        Set<String> r = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        if (r == null || r.isEmpty()) return List.of();
        return r.stream().map(UUID::fromString).toList();
    }

    /**
     * 지정된 시점 이전의 데이터를 삭제합니다.
     *
     * @param key 키
     * @param cutoffEpochMillis 컷오프 에포크 밀리초
     */
    public void pruneBefore(String key, long cutoffEpochMillis) {
        double max = (double) (cutoffEpochMillis - 1);
        redisTemplate.opsForZSet().removeRangeByScore(key, Double.NEGATIVE_INFINITY, max);
    }

    /**
     * 키의 만료 시간을 설정합니다.
     *
     * @param key 키
     * @param ttl TTL
     */
    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }
}
