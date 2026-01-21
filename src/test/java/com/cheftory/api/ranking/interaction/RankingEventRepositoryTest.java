package com.cheftory.api.ranking.interaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("RankingEventRepository Tests")
class RankingEventRepositoryTest extends DbContextTest {

    @Autowired
    private RankingEventRepository rankingEventRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist and read ranking event")
        void shouldPersistAndReadRankingEvent() {
            Clock clock = mock(Clock.class);
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            doReturn(now).when(clock).now();

            UUID userId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            UUID requestId = UUID.randomUUID();

            RankingEvent event = RankingEvent.create(
                    userId, RankingItemType.RECIPE, itemId, RankingEventType.VIEW, requestId, clock);

            rankingEventRepository.save(event);

            RankingEvent saved = rankingEventRepository.findById(event.getId()).orElseThrow();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getItemType()).isEqualTo(RankingItemType.RECIPE);
            assertThat(saved.getItemId()).isEqualTo(itemId);
            assertThat(saved.getEventType()).isEqualTo(RankingEventType.VIEW);
            assertThat(saved.getRequestId()).isEqualTo(requestId);
            assertThat(saved.getCreatedAt()).isEqualTo(now);
            assertThat(saved.getMarket()).isNotBlank();
            assertThat(saved.getCountryCode()).isNotBlank();
        }
    }
}
