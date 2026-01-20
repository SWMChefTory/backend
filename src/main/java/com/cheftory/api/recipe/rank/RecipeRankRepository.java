package com.cheftory.api.recipe.rank;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecipeRankRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRanking(String key, UUID recipeId, Integer rank) {
        redisTemplate.opsForZSet().add(key, recipeId.toString(), rank.doubleValue());
    }

    public void setExpire(String key, Duration duration) {
        redisTemplate.expire(key, duration);
    }

    public void saveLatest(String pointerKey, String realKey) {
        redisTemplate.opsForValue().set(pointerKey, realKey);
    }

    public Optional<String> findLatest(String pointerKey) {
        if (pointerKey == null || pointerKey.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(redisTemplate.opsForValue().get(pointerKey)).filter(key -> !key.isBlank());
    }

    public Set<String> findRecipeIds(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Long count(String key) {
        Long count = redisTemplate.opsForZSet().zCard(key);
        return count != null ? count : 0L;
    }

    public List<String> findRecipeIdsByRank(String key, int startRank, int count) {
        long startIndex = Math.max(0, (long) startRank - 1);
        long endIndex = startIndex + count - 1;

        Set<String> set = redisTemplate.opsForZSet().range(key, startIndex, endIndex);
        if (set == null || set.isEmpty()) return List.of();
        return List.copyOf(set);
    }
}
