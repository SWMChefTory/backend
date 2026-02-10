package com.cheftory.api.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.RankingCursor;
import com.cheftory.api._common.cursor.RankingCursorCodec;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.candidate.RankingCandidateService;
import com.cheftory.api.ranking.interaction.RankingInteractionService;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.ranking.personalization.RankingPersonalizationService;
import com.cheftory.api.ranking.snapshot.RankingSnapshotService;
import com.cheftory.api.search.query.SearchPage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingService Tests")
class RankingServiceTest {

    @Mock
    private RankingCandidateService rankingCandidateService;

    @Mock
    private RankingSnapshotService rankingSnapshotService;

    @Mock
    private RankingInteractionService rankingInteractionService;

    @Mock
    private RankingPersonalizationService rankingPersonalizationService;

    @Mock
    private RankingCursorCodec rankingCursorCodec;

    @InjectMocks
    private RankingService rankingService;

    @Test
    @DisplayName("recommend first page opens pit and refreshes snapshot when hasNext")
    void recommendFirstPageHasNext() throws CheftoryException {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        List<UUID> items = List.of(UUID.randomUUID(), UUID.randomUUID());
        PersonalizationProfile profile = new PersonalizationProfile(List.of("a"), List.of("b"));

        doReturn(requestId).when(rankingSnapshotService).issueRequestId();
        doReturn(null)
                .when(rankingSnapshotService)
                .getPit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);
        doReturn("pit-1").when(rankingCandidateService).openPit();
        doReturn(List.of(UUID.randomUUID()))
                .when(rankingInteractionService)
                .getRecentSeeds(userId, RankingItemType.RECIPE, 10);
        doReturn(profile).when(rankingPersonalizationService).aggregateProfile(any());
        doReturn(new SearchPage(items, "next"))
                .when(rankingCandidateService)
                .searchWithPit(RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, 2, profile, "pit-1", null);
        doReturn(0L).when(rankingSnapshotService).allocateImpressionPositions(requestId, items.size());
        doReturn("encoded-next").when(rankingCursorCodec).encode(any(RankingCursor.class));

        CursorPage<UUID> result =
                rankingService.recommend(userId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, null, 2);

        assertThat(result.items()).isEqualTo(items);
        assertThat(result.nextCursor()).isEqualTo("encoded-next");
        verify(rankingSnapshotService)
                .savePit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, "pit-1");
        verify(rankingSnapshotService).refreshPit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);
        verify(rankingCandidateService, never()).closePit(any());
        verify(rankingSnapshotService, never()).deletePit(any(), any(), any());
        verify(rankingSnapshotService, never()).clearImpressionPositions(any());
    }

    @Test
    @DisplayName("recommend next page closes pit and clears snapshot when no next cursor")
    void recommendNextPageNoNext() throws CheftoryException {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        RankingCursor decoded = new RankingCursor(requestId, "after");
        List<UUID> items = List.of(UUID.randomUUID());
        PersonalizationProfile profile = new PersonalizationProfile(List.of("a"), List.of("b"));

        doReturn(decoded).when(rankingCursorCodec).decode("cursor");
        doReturn("pit-1")
                .when(rankingSnapshotService)
                .getPit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);
        doReturn(List.of(UUID.randomUUID()))
                .when(rankingInteractionService)
                .getRecentSeeds(userId, RankingItemType.RECIPE, 10);
        doReturn(profile).when(rankingPersonalizationService).aggregateProfile(any());
        doReturn(new SearchPage(items, null))
                .when(rankingCandidateService)
                .searchWithPit(RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, 2, profile, "pit-1", "after");
        doReturn(0L).when(rankingSnapshotService).allocateImpressionPositions(requestId, items.size());

        CursorPage<UUID> result = rankingService.recommend(
                userId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, "cursor", 2);

        assertThat(result.items()).isEqualTo(items);
        assertThat(result.nextCursor()).isNull();
        verify(rankingCandidateService).closePit("pit-1");
        verify(rankingSnapshotService).deletePit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);
        verify(rankingSnapshotService).clearImpressionPositions(requestId);
        verify(rankingSnapshotService, never()).refreshPit(any(), any(), any());
        verify(rankingCandidateService, never()).openPit();
    }

    @Test
    @DisplayName("event delegates to interaction service")
    void eventDelegatesToInteractionService() throws CheftoryException {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        rankingService.event(userId, RankingItemType.RECIPE, itemId, RankingEventType.VIEW, requestId);

        verify(rankingInteractionService)
                .logEvent(userId, RankingItemType.RECIPE, itemId, RankingEventType.VIEW, requestId);
    }
}
