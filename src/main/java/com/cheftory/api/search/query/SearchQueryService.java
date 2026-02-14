package com.cheftory.api.search.query;

import com.cheftory.api._common.I18nTranslator;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.ScoreIdCursor;
import com.cheftory.api._common.cursor.ScoreIdCursorCodec;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 검색 쿼리 서비스.
 *
 * <p>검색 쿼리 관련 비즈니스 로직을 처리합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchQueryService {

    /** 검색 쿼리 OpenSearch 리포지토리. */
    private final SearchQueryRepository searchQueryRepository;

    /** 점수-ID 커서 코덱. */
    private final ScoreIdCursorCodec scoreIdCursorCodec;

    /** i18n 번역기. */
    private final I18nTranslator i18nTranslator;

    /** 커서 페이지 크기. */
    private static final int CURSOR_PAGE_SIZE = 20;

    /**
     * 키워드로 검색합니다.
     *
     * @param scope 검색 범위
     * @param text 검색어
     * @param cursor 커서
     * @return 검색 쿼리 커서 페이지
     * @throws SearchException 검색 예외
     */
    public CursorPage<SearchQuery> searchByKeyword(SearchQueryScope scope, String text, String cursor)
            throws SearchException {
        boolean first = (cursor == null || cursor.isBlank());
        Pageable pageable = PageRequest.of(0, CURSOR_PAGE_SIZE + 1);

        ScoreIdCursor scoreIdCursor;
        try {
            scoreIdCursor = first ? null : scoreIdCursorCodec.decode(cursor);
        } catch (CursorException exception) {
            throw new SearchException(SearchErrorCode.SEARCH_FAILED, exception);
        }

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

    /**
     * 히트의 점수를 반환합니다.
     *
     * @param hit 검색 결과 히트
     * @return 점수
     */
    private static double score(Hit<SearchQuery> hit) {
        return hit.score() != null ? hit.score() : 0.0;
    }

    /**
     * 후보군 검색을 위한 PIT를 엽니다.
     *
     * @return PIT ID
     * @throws SearchException 검색 예외
     */
    public String openPitForCandidates() throws SearchException {
        return searchQueryRepository.createPitId();
    }

    /**
     * PIT를 닫습니다.
     *
     * @param pitId PIT ID
     */
    public void closePit(String pitId) {
        searchQueryRepository.closePit(pitId);
    }

    /**
     * 여러 검색 쿼리를 조회합니다.
     *
     * @param ids 문서 ID 목록
     * @return 검색 쿼리 목록
     * @throws SearchException 검색 예외
     */
    public List<SearchQuery> mgetSearchQueries(List<String> ids) throws SearchException {
        return searchQueryRepository.mgetSearchQueries(ids);
    }

    /**
     * PIT를 사용하여 후보군을 검색합니다.
     *
     * @param surfaceType 랭킹 표면 타입
     * @param itemType 랭킹 아이템 타입
     * @param size 페이지 크기
     * @param profile 개인화 프로필
     * @param pitId PIT ID
     * @param cursor 커서
     * @return 검색 페이지
     * @throws SearchException 검색 예외
     */
    public SearchPage searchCandidatesWithPit(
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            int size,
            PersonalizationProfile profile,
            String pitId,
            String cursor)
            throws SearchException {

        boolean first = (cursor == null || cursor.isBlank());
        Pageable pageable = PageRequest.of(0, size + 1);

        ScoreIdCursor scoreIdCursor;
        try {
            scoreIdCursor = first ? null : scoreIdCursorCodec.decode(cursor);
        } catch (CursorException exception) {
            throw new SearchException(SearchErrorCode.SEARCH_FAILED, exception);
        }
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

    /**
     * 랭킹 아이템 타입을 검색 쿼리 범위로 변환합니다.
     *
     * @param itemType 랭킹 아이템 타입
     * @return 검색 쿼리 범위
     */
    private SearchQueryScope toScope(RankingItemType itemType) {
        return SearchQueryScope.RECIPE;
    }
}
