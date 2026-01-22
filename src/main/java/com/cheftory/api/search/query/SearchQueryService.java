package com.cheftory.api.search.query;

import com.cheftory.api._common.I18nTranslator;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.ScoreIdCursor;
import com.cheftory.api._common.cursor.ScoreIdCursorCodec;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.query.entity.SearchQuery;
import com.cheftory.api.search.utils.SearchPageRequest;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchQueryService {
    private final SearchQueryRepository searchQueryRepository;
    private final ScoreIdCursorCodec scoreIdCursorCodec;
    private final I18nTranslator i18nTranslator;
    private static final int CURSOR_PAGE_SIZE = 20;

    @Deprecated(forRemoval = true)
    public Page<SearchQuery> searchByKeyword(SearchQueryScope scope, String text, int page) {
        Pageable pageable = SearchPageRequest.create(page);
        return searchQueryRepository.searchByKeyword(scope, text, pageable);
    }

    public CursorPage<SearchQuery> searchByKeyword(SearchQueryScope scope, String text, String cursor) {
        boolean first = (cursor == null || cursor.isBlank());
        Pageable pageable = PageRequest.of(0, CURSOR_PAGE_SIZE + 1);

        ScoreIdCursor scoreIdCursor = first ? null : scoreIdCursorCodec.decode(cursor);

        String anchorNowIso = first ? scoreIdCursorCodec.newAnchorNow() : scoreIdCursor.anchorNowIso();
        String pitId = first ? searchQueryRepository.createPitId() : scoreIdCursor.pitId();

        List<Hit<SearchQuery>> rows = first
                ? searchQueryRepository.searchByKeywordCursorFirst(scope, text, anchorNowIso, pitId, pageable)
                : searchQueryRepository.searchByKeywordCursorKeyset(
                        scope, text, anchorNowIso, pitId, scoreIdCursor.score(), scoreIdCursor.id(), pageable);

        CursorPage<Hit<SearchQuery>> page = CursorPages.of(
                rows,
                CURSOR_PAGE_SIZE,
                last -> scoreIdCursorCodec.encode(new ScoreIdCursor(score(last), last.id(), anchorNowIso, pitId)));

        List<SearchQuery> content = page.items().stream().map(Hit::source).toList();
        return CursorPage.of(content, page.nextCursor());
    }

    private static double score(Hit<SearchQuery> hit) {
        return hit.score() != null ? hit.score() : 0.0;
    }

    public String openPitForCandidates() {
        return searchQueryRepository.createPitId();
    }

    public void closePit(String pitId) {
        searchQueryRepository.closePit(pitId);
    }

    public List<SearchQuery> mgetSearchQueries(List<String> ids) {
        return searchQueryRepository.mgetSearchQueries(ids);
    }

    public SearchPage searchCandidatesWithPit(
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            int size,
            PersonalizationProfile profile,
            String pitId,
            String cursor) {

        boolean first = (cursor == null || cursor.isBlank());
        Pageable pageable = PageRequest.of(0, size + 1);

        ScoreIdCursor scoreIdCursor = first ? null : scoreIdCursorCodec.decode(cursor);
        String anchorNowIso = first ? scoreIdCursorCodec.newAnchorNow() : scoreIdCursor.anchorNowIso();

        SearchQueryScope scope = toScope(itemType);

        String label = i18nTranslator.translate(surfaceType.messageKey());

        log.info(
                "[CAND] surface={} messageKey={} label='{}' itemType={} size={} pitId={} first={} cursor={}",
                surfaceType,
                surfaceType.messageKey(),
                label,
                itemType,
                size,
                pitId,
                first,
                cursor);
        log.info("[CAND] anchorNowIso='{}'", anchorNowIso);
        log.info("[CAND] profile keywordsTop={} channelsTop={}", profile.keywordsTop(), profile.channelsTop());

        List<Hit<SearchQuery>> rows = first
                ? searchQueryRepository.searchCandidatesCursorFirst(
                        scope, label, anchorNowIso, pitId, profile, pageable)
                : searchQueryRepository.searchCandidatesCursorKeyset(
                        scope,
                        label,
                        anchorNowIso,
                        pitId,
                        profile,
                        scoreIdCursor.score(),
                        scoreIdCursor.id(),
                        pageable);

        CursorPage<Hit<SearchQuery>> page = CursorPages.of(
                rows,
                size,
                last -> scoreIdCursorCodec.encode(new ScoreIdCursor(score(last), last.id(), anchorNowIso, pitId)));

        List<UUID> ids = page.items().stream()
                .map(Hit::id)
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .toList();

        return new SearchPage(ids, page.nextCursor());
    }

    private SearchQueryScope toScope(RankingItemType itemType) {
        return SearchQueryScope.RECIPE;
    }
}
