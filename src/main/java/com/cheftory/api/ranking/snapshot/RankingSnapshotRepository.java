package com.cheftory.api.ranking.snapshot;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingSnapshotRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveString(String key, String value, Duration ttl) {
        if (value == null || value.isBlank()) return;
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public Long incrementLong(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
}
