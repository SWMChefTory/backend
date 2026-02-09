package com.cheftory.api.ranking;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.RankingCursor;
import com.cheftory.api._common.cursor.RankingCursorCodec;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.candidate.RankingCandidateService;
import com.cheftory.api.ranking.interaction.RankingInteractionService;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.ranking.personalization.RankingPersonalizationService;
import com.cheftory.api.ranking.snapshot.RankingSnapshotService;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchPage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final RankingCandidateService rankingCandidateService;
    private final RankingSnapshotService rankingSnapshotService;
    private final RankingInteractionService rankingInteractionService;
    private final RankingPersonalizationService rankingPersonalizationService;
    private final RankingCursorCodec rankingCursorCodec;

    public CursorPage<UUID> recommend(
            UUID userId, RankingSurfaceType surfaceType, RankingItemType itemType, String cursor, int pageSize)
				throws CheftoryException {

        boolean first = (cursor == null || cursor.isBlank());

        final UUID requestId;
        final String searchAfter;

        if (first) {
            requestId = rankingSnapshotService.issueRequestId();
            searchAfter = null;
        } else {
            RankingCursor decoded = rankingCursorCodec.decode(cursor);
            requestId = decoded.requestId();
            searchAfter = decoded.searchAfter();
        }

        String pitId = rankingSnapshotService.getPit(requestId, surfaceType, itemType);

        if (pitId == null || pitId.isBlank()) {
            pitId = rankingCandidateService.openPit();
            rankingSnapshotService.savePit(requestId, surfaceType, itemType, pitId);
        }

        log.info(
                "[REC] userId={} surface={} itemType={} first={} requestId={} pitId={} searchAfter={}",
                userId,
                surfaceType,
                itemType,
                first,
                requestId,
                pitId,
                searchAfter);

        List<UUID> seeds = rankingInteractionService.getRecentSeeds(userId, itemType, 10);

        log.info("[REC] seeds(size={}): {}", seeds.size(), seeds);

        PersonalizationProfile profile = rankingPersonalizationService.aggregateProfile(seeds);

        log.info(
                "[REC] profile keywordsTop(size={}): {}",
                profile.keywordsTop() != null ? profile.keywordsTop().size() : -1,
                profile.keywordsTop());
        log.info(
                "[REC] profile channelsTop(size={}): {}",
                profile.channelsTop() != null ? profile.channelsTop().size() : -1,
                profile.channelsTop());

        SearchPage page =
                rankingCandidateService.searchWithPit(surfaceType, itemType, pageSize, profile, pitId, searchAfter);

        log.info(
                "[REC] page items(size={}) nextCursorBlank?={}",
                page.items().size(),
                (page.nextCursor() == null || page.nextCursor().isBlank()));
        log.info("[REC] top5 ids: {}", page.items().stream().limit(5).toList());

        long positionStart = rankingSnapshotService.allocateImpressionPositions(
                requestId, page.items().size());

        rankingInteractionService.logImpressions(userId, surfaceType, itemType, page.items(), requestId, positionStart);

        boolean hasNext = page.nextCursor() != null && !page.nextCursor().isBlank();

        if (!hasNext) {
            try {
                rankingCandidateService.closePit(pitId);
            } catch (Exception ignored) {
            }
            rankingSnapshotService.deletePit(requestId, surfaceType, itemType);
            rankingSnapshotService.clearImpressionPositions(requestId);
        } else {
            rankingSnapshotService.refreshPit(requestId, surfaceType, itemType);
        }

        return CursorPage.of(
                page.items(),
                hasNext ? rankingCursorCodec.encode(new RankingCursor(requestId, page.nextCursor())) : null);
    }

    public void event(UUID userId, RankingItemType itemType, UUID itemId, RankingEventType eventType, UUID requestId)
            throws CheftoryException {
        rankingInteractionService.logEvent(userId, itemType, itemId, eventType, requestId);
    }
}
