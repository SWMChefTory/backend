package com.cheftory.api.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.rank.port.RecipeRankEventType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeRankingAdapter Tests")
class RecipeRankingAdapterTest {

    @Mock
    private RankingService rankingService;

    @InjectMocks
    private RecipeRankingAdapter adapter;

    @Test
    @DisplayName("logEvent delegates to rankingService")
    void logEventDelegates() throws CheftoryException {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        adapter.logEvent(userId, recipeId, RecipeRankEventType.VIEW, requestId);

        verify(rankingService).event(userId, RankingItemType.RECIPE, recipeId, RankingEventType.VIEW, requestId);
    }

    @Test
    @DisplayName("recommend delegates to rankingService")
    void recommendDelegates() throws CheftoryException {
        UUID userId = UUID.randomUUID();
        CursorPage<UUID> expected = CursorPage.of(List.of(UUID.randomUUID()), "cursor");

        doReturn(expected)
                .when(rankingService)
                .recommend(userId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, "cursor", 10);

        CursorPage<UUID> result = adapter.recommend(userId, RecipeCuisineType.KOREAN, "cursor", 10);

        assertThat(result).isEqualTo(expected);
        verify(rankingService)
                .recommend(userId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, "cursor", 10);
    }
}
