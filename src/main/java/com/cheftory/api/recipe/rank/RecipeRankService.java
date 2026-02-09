package com.cheftory.api.recipe.rank;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.RankCursor;
import com.cheftory.api._common.cursor.RankCursorCodec;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipe.rank.exception.RecipeRankException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public CursorPage<UUID> getRecipeIds(RankingType type, String cursor) throws CheftoryException {
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

    public CursorPage<UUID> getCuisineRecipes(UUID userId, RecipeCuisineType type, String cursor)
            throws CheftoryException {
        final int limit = PAGE_SIZE;
        return recipeRankingPort.recommend(userId, toSurface(type), RankingItemType.RECIPE, cursor, limit);
    }

    public void logEvent(UUID userId, UUID recipeId, RankingEventType eventType) throws CheftoryException {
        recipeRankingPort.logEvent(userId, RankingItemType.RECIPE, recipeId, eventType, null);
    }

    private RankingSurfaceType toSurface(RecipeCuisineType type) {
        return switch (type) {
            case KOREAN -> RankingSurfaceType.CUISINE_KOREAN;
            case SNACK -> RankingSurfaceType.CUISINE_SNACK;
            case CHINESE -> RankingSurfaceType.CUISINE_CHINESE;
            case JAPANESE -> RankingSurfaceType.CUISINE_JAPANESE;
            case WESTERN -> RankingSurfaceType.CUISINE_WESTERN;
            case DESSERT -> RankingSurfaceType.CUISINE_DESSERT;
            case HEALTHY -> RankingSurfaceType.CUISINE_HEALTHY;
            case BABY -> RankingSurfaceType.CUISINE_BABY;
            case SIMPLE -> RankingSurfaceType.CUISINE_SIMPLE;
        };
    }
}
