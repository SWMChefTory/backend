package com.cheftory.api.ranking;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.RankingCursor;
import com.cheftory.api._common.cursor.RankingCursorCodec;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.candidate.RankingCandidatePage;
import com.cheftory.api.ranking.candidate.RankingCandidateService;
import com.cheftory.api.ranking.interaction.RankingInteractionService;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.ranking.personalization.RankingPersonalizationService;
import com.cheftory.api.ranking.snapshot.RankingSnapshotService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 랭킹 서비스.
 *
 * <p>개인화된 랭킹 추천 및 이벤트 추적 기능을 제공합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final RankingCandidateService rankingCandidateService;
    private final RankingSnapshotService rankingSnapshotService;
    private final RankingInteractionService rankingInteractionService;
    private final RankingPersonalizationService rankingPersonalizationService;
    private final RankingCursorCodec rankingCursorCodec;

    /**
     * 개인화된 랭킹 추천을 반환합니다.
     *
     * @param userId 사용자 ID
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     * @param cursor 커서
     * @param pageSize 페이지 크기
     * @return 커서 페이지
     * @throws CheftoryException Cheftory 예외
     */
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

        RankingCandidatePage page =
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

    /**
     * 랭킹 이벤트를 기록합니다.
     *
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @param itemId 아이템 ID
     * @param eventType 이벤트 타입
     * @param requestId 요청 ID
     * @throws CheftoryException Cheftory 예외
     */
    public void event(UUID userId, RankingItemType itemType, UUID itemId, RankingEventType eventType, UUID requestId)
            throws CheftoryException {
        rankingInteractionService.logEvent(userId, itemType, itemId, eventType, requestId);
    }
}
