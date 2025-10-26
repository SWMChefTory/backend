package com.cheftory.api.recipeinfo.rank;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

  public void setExpire(String key, Long seconds) {
    redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
  }

  public void saveLatest(String pointerKey, String realKey) {
    redisTemplate.opsForValue().set(pointerKey, realKey);
  }

  public Optional<String> findLatest(String pointerKey) {
    if (pointerKey == null || pointerKey.isBlank()) {
      return Optional.empty();
    }

    return Optional.ofNullable(redisTemplate.opsForValue().get(pointerKey))
        .filter(key -> !key.isBlank());
  }

  public Set<String> findRecipeIds(String key, long start, long end) {
    return redisTemplate.opsForZSet().range(key, start, end);
  }

  public Long count(String key) {
    Long count = redisTemplate.opsForZSet().zCard(key);
    return count != null ? count : 0L;
  }
}
