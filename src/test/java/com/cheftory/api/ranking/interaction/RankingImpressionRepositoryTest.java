package com.cheftory.api.ranking.interaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("RankingImpressionRepository Tests")
class RankingImpressionRepositoryTest extends DbContextTest {

    @Autowired
    private RankingImpressionRepository rankingImpressionRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist and read ranking impression")
        void shouldPersistAndReadRankingImpression() {
            Clock clock = mock(Clock.class);
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            doReturn(now).when(clock).now();

            UUID requestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            RankingImpression impression = RankingImpression.create(
                    requestId, userId, RankingItemType.RECIPE, itemId, RankingSurfaceType.CUISINE_BABY, 5, clock);

            rankingImpressionRepository.save(impression);

            RankingImpression saved =
                    rankingImpressionRepository.findById(impression.getId()).orElseThrow();
            assertThat(saved.getRequestId()).isEqualTo(requestId);
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getItemType()).isEqualTo(RankingItemType.RECIPE);
            assertThat(saved.getItemId()).isEqualTo(itemId);
            assertThat(saved.getSurfaceType()).isEqualTo(RankingSurfaceType.CUISINE_BABY);
            assertThat(saved.getPosition()).isEqualTo(5);
            assertThat(saved.getCreatedAt()).isEqualTo(now);
            assertThat(saved.getMarket()).isNotBlank();
            assertThat(saved.getCountryCode()).isNotBlank();
        }
    }
}
