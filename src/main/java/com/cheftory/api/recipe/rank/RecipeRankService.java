package com.cheftory.api.recipe.rank;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.RankCursor;
import com.cheftory.api._common.cursor.RankCursorCodec;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.rank.entity.RecipeRanking;
import com.cheftory.api.recipe.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipe.rank.exception.RecipeRankException;
import com.cheftory.api.recipe.rank.port.RecipeRankingPort;
import com.cheftory.api.recipe.rank.repository.RecipeRankRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 랭킹 서비스.
 *
 * <p>레시피 랭킹 정보를 조회하고 관리합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeRankService {
    private final RecipeRankRepository repository;
    private final RankCursorCodec cursorCodec;
    private final RecipeRankingPort port;
    private final Clock clock;

    private static final Integer PAGE_SIZE = 10;

    /**
     * 레시피 랭킹 정보를 업데이트합니다.
     *
     * @param type 랭킹 타입
     * @param recipeIds 랭킹에 추가할 레시피 ID 목록 (순서가 랭킹 순위)
     */
    public void updateRecipes(RankingType type, List<UUID> recipeIds) {
        RecipeRanking ranking = RecipeRanking.create(type, clock);

        IntStream.range(0, recipeIds.size())
                .boxed()
                .forEach(i -> repository.saveRanking(ranking.getKey(), recipeIds.get(i), i + 1));

        repository.setExpire(ranking.getKey(), ranking.getTtl());
        repository.saveLatest(ranking.getLatestPointerKey(), ranking.getKey());
    }

    /**
     * 랭킹 타입별 레시피 ID 목록을 커서 기반 페이징으로 조회합니다.
     *
     * @param type 랭킹 타입
     * @param cursor 페이징 커서
     * @return 레시피 ID 목록
     * @throws CheftoryException 랭킹 정보를 찾을 수 없는 경우
     */
    public CursorPage<UUID> getRecipeIds(RankingType type, String cursor) throws CheftoryException {
        final int limit = PAGE_SIZE;
        final int fetch = limit + 1;
        final boolean first = (cursor == null || cursor.isBlank());

        final RankCursor rankCursor = first ? null : cursorCodec.decode(cursor);

        final String rankingKey = first
                ? repository
                        .findLatest(RecipeRanking.getLatestPointerKey(type))
                        .orElseThrow(() -> new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND))
                : rankCursor.rankingKey();

        final int startRank = first ? 1 : rankCursor.lastRank() + 1;

        final List<UUID> rows = repository.findRecipeIdsByRank(rankingKey, startRank, fetch).stream()
                .map(UUID::fromString)
                .toList();

        return CursorPages.of(
                rows, limit, lastItem -> cursorCodec.encode(new RankCursor(rankingKey, startRank + limit - 1)));
    }

    /**
     * 요리 타입별 레시피 추천 목록을 커서 기반 페이징으로 조회합니다.
     *
     * @param userId 사용자 ID
     * @param type 요리 타입
     * @param cursor 페이징 커서
     * @return 레시피 ID 목록
     * @throws CheftoryException 추천 조회 실패 시
     */
    public CursorPage<UUID> getCuisineRecipes(UUID userId, RecipeCuisineType type, String cursor)
            throws CheftoryException {
        final int limit = PAGE_SIZE;
        return port.recommend(userId, toSurface(type), RankingItemType.RECIPE, cursor, limit);
    }

    /**
     * 레시피 관련 이벤트를 로깅합니다.
     *
     * <p>조회, 클릭 등의 사용자 행동을 랭킹 시스템에 기록합니다.</p>
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param eventType 이벤트 타입
     * @throws CheftoryException 이벤트 로깅 실패 시
     */
    public void logEvent(UUID userId, UUID recipeId, RankingEventType eventType) throws CheftoryException {
        port.logEvent(userId, RankingItemType.RECIPE, recipeId, eventType, null);
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
