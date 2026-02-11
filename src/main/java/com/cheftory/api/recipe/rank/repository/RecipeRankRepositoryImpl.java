package com.cheftory.api.recipe.rank.repository;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.RankCursor;
import com.cheftory.api._common.cursor.RankCursorCodec;
import com.cheftory.api.recipe.rank.RankingType;
import com.cheftory.api.recipe.rank.entity.RecipeRanking;
import com.cheftory.api.recipe.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipe.rank.exception.RecipeRankException;
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
public class RecipeRankRepositoryImpl implements RecipeRankRepository {

    private static final Integer PAGE_SIZE = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final RankCursorCodec cursorCodec;

    @Override
    public void saveRanking(String key, UUID recipeId, Integer rank) {
        redisTemplate.opsForZSet().add(key, recipeId.toString(), rank.doubleValue());
    }

    @Override
    public void setExpire(String key, Duration duration) {
        redisTemplate.expire(key, duration);
    }

    @Override
    public void saveLatest(String pointerKey, String realKey) {
        redisTemplate.opsForValue().set(pointerKey, realKey);
    }

    @Override
    public Optional<String> findLatest(String pointerKey) {
        if (pointerKey == null || pointerKey.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(redisTemplate.opsForValue().get(pointerKey)).filter(key -> !key.isBlank());
    }

    @Override
    public Set<String> findRecipeIds(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    @Override
    public Long count(String key) {
        Long count = redisTemplate.opsForZSet().zCard(key);
        return count != null ? count : 0L;
    }

    @Override
    public List<String> findRecipeIdsByRank(String key, int startRank, int count) {
        long startIndex = Math.max(0, (long) startRank - 1);
        long endIndex = startIndex + count - 1;

        Set<String> set = redisTemplate.opsForZSet().range(key, startIndex, endIndex);
        if (set == null || set.isEmpty()) return List.of();
        return List.copyOf(set);
    }

    @Override
    public CursorPage<UUID> getRecipeIdsFirst(RankingType type) throws RecipeRankException {
        String rankingKey = findLatest(RecipeRanking.getLatestPointerKey(type))
                .orElseThrow(() -> new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND));

        List<UUID> rows = findRecipeIdsByRank(rankingKey, 1, PAGE_SIZE + 1).stream()
                .map(UUID::fromString)
                .toList();

        return CursorPages.of(
                rows, PAGE_SIZE, lastItem -> cursorCodec.encode(new RankCursor(rankingKey, 1 + PAGE_SIZE - 1)));
    }

    @Override
    public CursorPage<UUID> getRecipeIds(RankingType type, String cursor) {
        RankCursor rankCursor = cursorCodec.decode(cursor);

        List<UUID> rows =
                findRecipeIdsByRank(rankCursor.rankingKey(), rankCursor.lastRank() + 1, PAGE_SIZE + 1).stream()
                        .map(UUID::fromString)
                        .toList();

        return CursorPages.of(
                rows,
                PAGE_SIZE,
                lastItem ->
                        cursorCodec.encode(new RankCursor(rankCursor.rankingKey(), rankCursor.lastRank() + PAGE_SIZE)));
    }
}
