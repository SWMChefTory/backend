package com.cheftory.api.search.history;

import com.cheftory.api._common.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String key, String searchText, Clock clock) {
        long score = clock.nowMillis();
        redisTemplate.opsForZSet().add(key, searchText, score);
    }

    public List<String> findRecent(String key, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        Set<String> history = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }

    public void remove(String key, String searchText) {
        redisTemplate.opsForZSet().remove(key, searchText);
    }

    public void removeOldEntries(String key, int maxSize) {
        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > maxSize) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - maxSize - 1);
        }
    }

    public void setExpire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }

    public void deleteAll(String key) {
        redisTemplate.delete(key);
    }
}
