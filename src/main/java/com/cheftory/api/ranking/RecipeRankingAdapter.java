package com.cheftory.api.ranking;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipe.rank.exception.RecipeRankException;
import com.cheftory.api.recipe.rank.port.RecipeRankEventType;
import com.cheftory.api.recipe.rank.port.RecipeRankingPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 레시피 랭킹 어댑터.
 *
 * <p>RecipeRankingPort 인터페이스를 구현하여 랭킹 서비스를 제공합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class RecipeRankingAdapter implements RecipeRankingPort {

    /** 랭킹 서비스 */
    private final RankingService rankingService;

    /**
     * 랭킹 이벤트를 기록합니다.
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param eventType 이벤트 타입
     * @param requestId 요청 ID
     * @throws RecipeRankException 처리 예외
     */
    @Override
    public void logEvent(UUID userId, UUID recipeId, RecipeRankEventType eventType, UUID requestId)
            throws RecipeRankException {
        try {
            rankingService.event(userId, RankingItemType.RECIPE, recipeId, toRankingEvent(eventType), requestId);
        } catch (CheftoryException exception) {
            throw new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_EVENT_FAILED, exception);
        }
    }

    /**
     * 개인화된 랭킹 추천을 반환합니다.
     *
     * @param userId 사용자 ID
     * @param cuisineType 요리 타입
     * @param cursor 커서
     * @param pageSize 페이지 크기
     * @return 커서 페이지
     * @throws RecipeRankException 처리 예외
     */
    @Override
    public CursorPage<UUID> recommend(UUID userId, RecipeCuisineType cuisineType, String cursor, int pageSize)
            throws RecipeRankException {
        try {
            return rankingService.recommend(userId, toSurface(cuisineType), RankingItemType.RECIPE, cursor, pageSize);
        } catch (CheftoryException exception) {
            throw new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_RECOMMEND_FAILED, exception);
        }
    }

    private RankingEventType toRankingEvent(RecipeRankEventType eventType) {
        return switch (eventType) {
            case VIEW -> RankingEventType.VIEW;
            case CATEGORIES -> RankingEventType.CATEGORIES;
        };
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
