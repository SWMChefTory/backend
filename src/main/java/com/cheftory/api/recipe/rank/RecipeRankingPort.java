package com.cheftory.api.recipe.rank;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.util.UUID;

public interface RecipeRankingPort {

    void logEvent(UUID userId, RankingItemType itemType, UUID itemId, RankingEventType eventType, UUID requestId);

    CursorPage<UUID> recommend(
            UUID userId, RankingSurfaceType surfaceType, RankingItemType itemType, String cursor, int pageSize);
}
