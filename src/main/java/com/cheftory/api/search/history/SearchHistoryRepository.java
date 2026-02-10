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

/**
 * 검색 히스토리 Redis 리포지토리.
 *
 * <p>Redis Sorted Set을 사용하여 검색 히스토리를 관리합니다.</p>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 검색어를 저장합니다.
     *
     * @param key 키
     * @param searchText 검색어
     * @param clock 클럭
     */
    public void save(String key, String searchText, Clock clock) {
        long score = clock.nowMillis();
        redisTemplate.opsForZSet().add(key, searchText, score);
    }

    /**
     * 최근 검색어를 조회합니다.
     *
     * @param key 키
     * @param limit 최대 개수
     * @return 최근 검색어 목록
     */
    public List<String> findRecent(String key, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        Set<String> history = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }

    /**
     * 검색어를 삭제합니다.
     *
     * @param key 키
     * @param searchText 검색어
     */
    public void remove(String key, String searchText) {
        redisTemplate.opsForZSet().remove(key, searchText);
    }

    /**
     * 오래된 항목을 삭제합니다.
     *
     * @param key 키
     * @param maxSize 최대 크기
     */
    public void removeOldEntries(String key, int maxSize) {
        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > maxSize) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - maxSize - 1);
        }
    }

    /**
     * 만료 시간을 설정합니다.
     *
     * @param key 키
     * @param ttl TTL
     */
    public void setExpire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }

    /**
     * 모든 데이터를 삭제합니다.
     *
     * @param key 키
     */
    public void deleteAll(String key) {
        redisTemplate.delete(key);
    }
}
