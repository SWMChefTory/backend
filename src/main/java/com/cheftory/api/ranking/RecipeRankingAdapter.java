package com.cheftory.api.ranking;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.rank.RecipeRankingPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeRankingAdapter implements RecipeRankingPort {

    private final RankingService rankingService;

    @Override
    public void logEvent(
            UUID userId, RankingItemType itemType, UUID itemId, RankingEventType eventType, UUID requestId) {
        rankingService.event(userId, itemType, itemId, eventType, requestId);
    }

    @Override
    public CursorPage<UUID> recommend(
            UUID userId, RankingSurfaceType surfaceType, RankingItemType itemType, String cursor, int pageSize) {
        return rankingService.recommend(userId, surfaceType, itemType, cursor, pageSize);
    }
}
