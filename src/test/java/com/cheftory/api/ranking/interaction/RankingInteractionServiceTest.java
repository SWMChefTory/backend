package com.cheftory.api.ranking.interaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.cheftory.api._common.Clock;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("RankingInteractionService Tests")
class RankingInteractionServiceTest {

    private RankingEventRepository rankingEventRepository;
    private RankingImpressionRepository rankingImpressionRepository;
    private RankingInteractionKeyGenerator keyGenerator;
    private RankingInteractionRepository rankingInteractionRepository;
    private Clock clock;
    private RankingInteractionService service;

    @BeforeEach
    void setUp() {
        rankingEventRepository = mock(RankingEventRepository.class);
        rankingImpressionRepository = mock(RankingImpressionRepository.class);
        keyGenerator = mock(RankingInteractionKeyGenerator.class);
        rankingInteractionRepository = mock(RankingInteractionRepository.class);
        clock = mock(Clock.class);
        service = new RankingInteractionService(
                rankingEventRepository, rankingImpressionRepository, keyGenerator, rankingInteractionRepository, clock);
    }

    @Nested
    @DisplayName("logEvent")
    class LogEvent {

        @Test
        @DisplayName("should save event and update recent views for VIEW event")
        void shouldSaveEventAndUpdateRecentViewsForViewEvent() throws CheftoryException {
            UUID userId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            UUID requestId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.of(2024, 1, 2, 10, 0, 0);
            long nowMillis = 1_700_000_100_000L;
            String recentKey = "recent:key";

            doReturn(now).when(clock).now();
            doReturn(nowMillis).when(clock).nowMillis();
            doReturn(recentKey).when(keyGenerator).recentViewsKey(userId, RankingItemType.RECIPE);

            service.logEvent(userId, RankingItemType.RECIPE, itemId, RankingEventType.VIEW, requestId);

            ArgumentCaptor<RankingEvent> eventCaptor = ArgumentCaptor.forClass(RankingEvent.class);
            verify(rankingEventRepository).save(eventCaptor.capture());
            RankingEvent saved = eventCaptor.getValue();

            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getItemType()).isEqualTo(RankingItemType.RECIPE);
            assertThat(saved.getItemId()).isEqualTo(itemId);
            assertThat(saved.getEventType()).isEqualTo(RankingEventType.VIEW);
            assertThat(saved.getRequestId()).isEqualTo(requestId);
            assertThat(saved.getCreatedAt()).isEqualTo(now);

            verify(rankingInteractionRepository).add(recentKey, itemId, nowMillis);
            long expectedCutoff = nowMillis - Duration.ofDays(30).toMillis();
            verify(rankingInteractionRepository).pruneBefore(recentKey, expectedCutoff);
            verify(rankingInteractionRepository).expire(recentKey, Duration.ofDays(60));
        }

        @Test
        @DisplayName("should only save event for non-VIEW event")
        void shouldOnlySaveEventForNonViewEvent() throws CheftoryException {
            UUID userId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            UUID requestId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.of(2024, 1, 2, 10, 0, 0);

            doReturn(now).when(clock).now();

            service.logEvent(userId, RankingItemType.RECIPE, itemId, RankingEventType.CATEGORIES, requestId);

            verify(rankingEventRepository).save(org.mockito.ArgumentMatchers.any(RankingEvent.class));
            verifyNoInteractions(rankingInteractionRepository);
        }
    }

    @Nested
    @DisplayName("getRecentSeeds")
    class GetRecentSeeds {

        @Test
        @DisplayName("should return recent seeds")
        void shouldReturnRecentSeeds() throws CheftoryException {
            UUID userId = UUID.randomUUID();
            String recentKey = "recent:key";
            List<UUID> expected = List.of(UUID.randomUUID(), UUID.randomUUID());

            doReturn(recentKey).when(keyGenerator).recentViewsKey(userId, RankingItemType.RECIPE);
            doReturn(expected).when(rankingInteractionRepository).getLatest(recentKey, 10);

            List<UUID> result = service.getRecentSeeds(userId, RankingItemType.RECIPE, 10);

            assertThat(result).isEqualTo(expected);
        }
    }
}
