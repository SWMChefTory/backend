package com.cheftory.api.ranking.interaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RankingImpression Tests")
class RankingImpressionTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create impression with expected fields")
        void shouldCreateImpressionWithExpectedFields() {
            Clock clock = mock(Clock.class);
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            doReturn(now).when(clock).now();

            UUID requestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            RankingImpression impression = RankingImpression.create(
                    requestId, userId, RankingItemType.RECIPE, itemId, RankingSurfaceType.CUISINE_BABY, 3, clock);

            assertThat(impression.getId()).isNotNull();
            assertThat(impression.getRequestId()).isEqualTo(requestId);
            assertThat(impression.getUserId()).isEqualTo(userId);
            assertThat(impression.getItemType()).isEqualTo(RankingItemType.RECIPE);
            assertThat(impression.getItemId()).isEqualTo(itemId);
            assertThat(impression.getSurfaceType()).isEqualTo(RankingSurfaceType.CUISINE_BABY);
            assertThat(impression.getPosition()).isEqualTo(3);
            assertThat(impression.getCreatedAt()).isEqualTo(now);
        }
    }
}
