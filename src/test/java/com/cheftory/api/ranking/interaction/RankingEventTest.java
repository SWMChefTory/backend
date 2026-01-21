package com.cheftory.api.ranking.interaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RankingEvent Tests")
class RankingEventTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create event with expected fields")
        void shouldCreateEventWithExpectedFields() {
            Clock clock = mock(Clock.class);
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            doReturn(now).when(clock).now();

            UUID userId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            UUID requestId = UUID.randomUUID();

            RankingEvent event = RankingEvent.create(
                    userId, RankingItemType.RECIPE, itemId, RankingEventType.VIEW, requestId, clock);

            assertThat(event.getId()).isNotNull();
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getItemType()).isEqualTo(RankingItemType.RECIPE);
            assertThat(event.getItemId()).isEqualTo(itemId);
            assertThat(event.getEventType()).isEqualTo(RankingEventType.VIEW);
            assertThat(event.getRequestId()).isEqualTo(requestId);
            assertThat(event.getCreatedAt()).isEqualTo(now);
        }
    }
}
