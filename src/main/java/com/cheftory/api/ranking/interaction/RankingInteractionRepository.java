package com.cheftory.api.ranking.interaction;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingInteractionRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void add(String key, UUID itemId, long epochMillis) {
        redisTemplate.opsForZSet().add(key, itemId.toString(), (double) epochMillis);
    }

    public List<UUID> getLatest(String key, long limit) {
        Set<String> r = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        if (r == null || r.isEmpty()) return List.of();
        return r.stream().map(UUID::fromString).toList();
    }

    public void pruneBefore(String key, long cutoffEpochMillis) {
        double max = (double) (cutoffEpochMillis - 1);
        redisTemplate.opsForZSet().removeRangeByScore(key, Double.NEGATIVE_INFINITY, max);
    }

    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }
}
