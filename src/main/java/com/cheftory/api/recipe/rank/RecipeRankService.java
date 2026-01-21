package com.cheftory.api.recipe.rank;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.RankCursor;
import com.cheftory.api._common.cursor.RankCursorCodec;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipe.rank.exception.RecipeRankException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeRankService {
    private final RecipeRankRepository recipeRankRepository;
    private final RankingKeyGenerator rankingKeyGenerator;
    private final RankCursorCodec rankCursorCodec;
    private final RecipeRankingPort recipeRankingPort;

    private static final Integer PAGE_SIZE = 10;
    private static final Duration TTL = Duration.ofDays(2);

    public void updateRecipes(RankingType type, List<UUID> recipeIds) {
        String newKey = rankingKeyGenerator.generateKey(type);

        IntStream.range(0, recipeIds.size())
                .boxed()
                .forEach(i -> recipeRankRepository.saveRanking(newKey, recipeIds.get(i), i + 1));

        recipeRankRepository.setExpire(newKey, TTL);
        recipeRankRepository.saveLatest(rankingKeyGenerator.getLatestKey(type), newKey);
    }

    @Deprecated(forRemoval = true)
    public Page<UUID> getRecipeIds(RankingType type, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        String latestPointerKey = rankingKeyGenerator.getLatestKey(type);

        String actualRankingKey = recipeRankRepository
                .findLatest(latestPointerKey)
                .orElseThrow(() -> new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND));

        long offset = pageable.getOffset();
        long limitEnd = offset + pageable.getPageSize() - 1;

        Set<String> ids = recipeRankRepository.findRecipeIds(actualRankingKey, offset, limitEnd);

        List<UUID> recipeIds = (ids == null || ids.isEmpty())
                ? List.of()
                : ids.stream().map(UUID::fromString).toList();

        String latestKey = recipeRankRepository
                .findLatest(rankingKeyGenerator.getLatestKey(type))
                .orElseThrow(() -> new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND));
        Long totalElements = recipeRankRepository.count(latestKey);

        return new PageImpl<>(recipeIds, pageable, totalElements);
    }

    public CursorPage<UUID> getRecipeIds(RankingType type, String cursor) {
        final int limit = PAGE_SIZE;
        final int fetch = limit + 1;
        final boolean first = (cursor == null || cursor.isBlank());

        final RankCursor rankCursor = first ? null : rankCursorCodec.decode(cursor);

        final String rankingKey = first
                ? recipeRankRepository
                        .findLatest(rankingKeyGenerator.getLatestKey(type))
                        .orElseThrow(() -> new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND))
                : rankCursor.rankingKey();

        final int startRank = first ? 1 : rankCursor.lastRank() + 1;

        final List<UUID> rows = recipeRankRepository.findRecipeIdsByRank(rankingKey, startRank, fetch).stream()
                .map(UUID::fromString)
                .toList();

        return CursorPages.of(
                rows, limit, lastItem -> rankCursorCodec.encode(new RankCursor(rankingKey, startRank + limit - 1)));
    }

    public CursorPage<UUID> getCuisineRecipes(UUID userId, RecipeCuisineType type, String cursor) {
        final int limit = PAGE_SIZE;
        return recipeRankingPort.recommend(userId, toSurface(type), RankingItemType.RECIPE, cursor, limit);
    }

    private RankingSurfaceType toSurface(RecipeCuisineType type) {
        return RankingSurfaceType.valueOf("CUISINE_" + type.name());
    }
}
