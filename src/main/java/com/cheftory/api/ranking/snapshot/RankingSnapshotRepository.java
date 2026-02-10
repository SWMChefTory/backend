package com.cheftory.api.ranking.snapshot;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 랭킹 스냅샷 Redis 리포지토리.
 *
 * <p>Redis를 사용하여 랭킹 스냅샷을 캐싱합니다.</p>
 */
@Repository
@RequiredArgsConstructor
public class RankingSnapshotRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 문자열 값을 저장합니다.
     *
     * @param key 키
     * @param value 값
     * @param ttl TTL
     */
    public void saveString(String key, String value, Duration ttl) {
        if (value == null || value.isBlank()) return;
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 문자열 값을 조회합니다.
     *
     * @param key 키
     * @return 값
     */
    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
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

    /**
     * 키를 삭제합니다.
     *
     * @param key 키
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Long 값을 증가시킵니다.
     *
     * @param key 키
     * @param delta 증가량
     * @return 증가 후 값
     */
    public Long incrementLong(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
}
