package com.cheftory.api.search.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.I18nTranslator;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.ScoreIdCursor;
import com.cheftory.api._common.cursor.ScoreIdCursorCodec;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchQueryService Tests")
class SearchQueryServiceTest {

    @Mock
    private SearchQueryRepository searchQueryRepository;

    @Mock
    private ScoreIdCursorCodec scoreIdCursorCodec;

    @Mock
    private I18nTranslator i18nTranslator;

    @InjectMocks
    private SearchQueryService searchQueryService;

    @Test
    @DisplayName("cursor가 없으면 첫 페이지를 조회한다")
    void shouldSearchFirstPageWithCursor() throws SearchException {
        String keyword = "김치찌개";
        String anchorNow = "2024-01-01T10:00:00";
        String pitId = "pit-1";

        doReturn(anchorNow).when(scoreIdCursorCodec).newAnchorNow();
        doReturn(pitId).when(searchQueryRepository).createPitId();

        List<Hit<Object>> rows = IntStream.range(0, 21)
                .mapToObj(i -> Hit.of(h -> h.id("id-" + i)
                        .score(1.0)
                        .source(SearchQuery.builder().id("id-" + i).build())))
                .toList();

        doReturn(rows)
                .when(searchQueryRepository)
                .searchByKeywordCursorFirst(
                        eq(SearchQueryScope.RECIPE), eq(keyword), eq(anchorNow), eq(pitId), any(Pageable.class));
        doReturn("next-cursor").when(scoreIdCursorCodec).encode(any(ScoreIdCursor.class));

        CursorPage<SearchQuery> result = searchQueryService.searchByKeyword(SearchQueryScope.RECIPE, keyword, null);

        assertThat(result.items()).hasSize(20);
        assertThat(result.nextCursor()).isEqualTo("next-cursor");
        verify(searchQueryRepository)
                .searchByKeywordCursorFirst(
                        eq(SearchQueryScope.RECIPE), eq(keyword), eq(anchorNow), eq(pitId), any(Pageable.class));
    }

    @Test
    @DisplayName("cursor가 있으면 keyset을 조회한다")
    void shouldSearchWithCursorKeyset() throws SearchException, CursorException {
        String keyword = "김치찌개";
        ScoreIdCursor decoded = new ScoreIdCursor(1.2, "id-10", "now", "pit");

        doReturn(decoded).when(scoreIdCursorCodec).decode("cursor");
        doReturn(List.of(Hit.of(h ->
                        h.id("id-11").source(SearchQuery.builder().id("id-11").build()))))
                .when(searchQueryRepository)
                .searchByKeywordCursorKeyset(
                        eq(SearchQueryScope.RECIPE),
                        eq(keyword),
                        eq(decoded.anchorNowIso()),
                        eq(decoded.pitId()),
                        eq(decoded.score()),
                        eq(decoded.id()),
                        any(Pageable.class));

        CursorPage<SearchQuery> result = searchQueryService.searchByKeyword(SearchQueryScope.RECIPE, keyword, "cursor");

        assertThat(result.items()).hasSize(1);
        verify(searchQueryRepository)
                .searchByKeywordCursorKeyset(
                        eq(SearchQueryScope.RECIPE),
                        eq(keyword),
                        eq(decoded.anchorNowIso()),
                        eq(decoded.pitId()),
                        eq(decoded.score()),
                        eq(decoded.id()),
                        any(Pageable.class));
    }

    @Test
    @DisplayName("openPitForCandidates는 PIT를 연다")
    void openPitForCandidatesOpensPit() throws SearchException {
        doReturn("pit-1").when(searchQueryRepository).createPitId();

        String result = searchQueryService.openPitForCandidates();

        assertThat(result).isEqualTo("pit-1");
        verify(searchQueryRepository).createPitId();
    }

    @Test
    @DisplayName("closePit은 PIT를 닫는다")
    void closePitClosesPit() {
        searchQueryService.closePit("pit-1");

        verify(searchQueryRepository).closePit("pit-1");
    }

    @Test
    @DisplayName("mgetSearchQueries는 repository를 위임한다")
    void mgetSearchQueriesDelegatesToRepository() throws SearchException {
        List<String> ids = List.of("id-1", "id-2");
        List<SearchQuery> expected = List.of(SearchQuery.builder().id("id-1").build());
        doReturn(expected).when(searchQueryRepository).mgetSearchQueries(ids);

        List<SearchQuery> result = searchQueryService.mgetSearchQueries(ids);

        assertThat(result).isEqualTo(expected);
        verify(searchQueryRepository).mgetSearchQueries(ids);
    }

    @Test
    @DisplayName("추천 후보 첫 페이지는 cursor를 생성한다")
    void searchCandidatesWithPitFirstPage() throws SearchException {
        RankingSurfaceType surfaceType = RankingSurfaceType.CUISINE_KOREAN;
        PersonalizationProfile profile = new PersonalizationProfile(List.of("kimchi"), List.of("channel"));
        String pitId = "pit-1";
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        doReturn("label").when(i18nTranslator).translate(surfaceType.messageKey());
        doReturn("anchor-now").when(scoreIdCursorCodec).newAnchorNow();

        List<Hit<SearchQuery>> rows = List.of(
                Hit.of(h -> h.id(id1.toString())
                        .score(2.0)
                        .source(SearchQuery.builder().id(id1.toString()).build())),
                Hit.of(h -> h.id(id2.toString())
                        .score(1.0)
                        .source(SearchQuery.builder().id(id2.toString()).build())),
                Hit.of(h -> h.id(UUID.randomUUID().toString())
                        .score(0.5)
                        .source(SearchQuery.builder().build())));

        doReturn(rows)
                .when(searchQueryRepository)
                .searchCandidatesCursorFirst(
                        eq(SearchQueryScope.RECIPE),
                        eq("label"),
                        eq("anchor-now"),
                        eq(pitId),
                        eq(profile),
                        any(Pageable.class));
        doReturn("next-cursor").when(scoreIdCursorCodec).encode(any(ScoreIdCursor.class));

        SearchPage result = searchQueryService.searchCandidatesWithPit(
                surfaceType, RankingItemType.RECIPE, 2, profile, pitId, null);

        assertThat(result.items()).containsExactly(id1, id2);
        assertThat(result.nextCursor()).isEqualTo("next-cursor");
        verify(searchQueryRepository)
                .searchCandidatesCursorFirst(
                        eq(SearchQueryScope.RECIPE),
                        eq("label"),
                        eq("anchor-now"),
                        eq(pitId),
                        eq(profile),
                        any(Pageable.class));
    }

    @Test
    @DisplayName("추천 후보 keyset 조회는 cursor를 갱신한다")
    void searchCandidatesWithPitKeyset() throws SearchException, CursorException {
        RankingSurfaceType surfaceType = RankingSurfaceType.CUISINE_KOREAN;
        PersonalizationProfile profile = new PersonalizationProfile(List.of("kimchi"), List.of("channel"));
        String pitId = "pit-1";
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        ScoreIdCursor decoded = new ScoreIdCursor(1.5, "id-10", "anchor-now", pitId);

        doReturn(decoded).when(scoreIdCursorCodec).decode("cursor");
        doReturn("label").when(i18nTranslator).translate(surfaceType.messageKey());
        List<Hit<SearchQuery>> rows = List.of(
                Hit.of(h -> h.id(id1.toString())
                        .score(1.0)
                        .source(SearchQuery.builder().id(id1.toString()).build())),
                Hit.of(h -> h.id(id2.toString())
                        .score(0.9)
                        .source(SearchQuery.builder().id(id2.toString()).build())),
                Hit.of(h -> h.id(UUID.randomUUID().toString())
                        .score(0.8)
                        .source(SearchQuery.builder().build())));
        doReturn(rows)
                .when(searchQueryRepository)
                .searchCandidatesCursorKeyset(
                        eq(SearchQueryScope.RECIPE),
                        eq("label"),
                        eq(decoded.anchorNowIso()),
                        eq(pitId),
                        eq(profile),
                        eq(decoded.score()),
                        eq(decoded.id()),
                        any(Pageable.class));
        doReturn("next-cursor").when(scoreIdCursorCodec).encode(any(ScoreIdCursor.class));

        SearchPage result = searchQueryService.searchCandidatesWithPit(
                surfaceType, RankingItemType.RECIPE, 2, profile, pitId, "cursor");

        assertThat(result.items()).containsExactly(id1, id2);
        assertThat(result.nextCursor()).isEqualTo("next-cursor");
        verify(searchQueryRepository)
                .searchCandidatesCursorKeyset(
                        eq(SearchQueryScope.RECIPE),
                        eq("label"),
                        eq(decoded.anchorNowIso()),
                        eq(pitId),
                        eq(profile),
                        eq(decoded.score()),
                        eq(decoded.id()),
                        any(Pageable.class));
    }

    @Test
    @DisplayName("키워드 검색에서 커서가 유효하지 않으면 SEARCH_FAILED를 던진다")
    void searchByKeywordInvalidCursor() throws CursorException {
        doThrow(new CursorException(com.cheftory.api._common.cursor.CursorErrorCode.INVALID_CURSOR))
                .when(scoreIdCursorCodec)
                .decode("invalid");

        assertThatThrownBy(() -> searchQueryService.searchByKeyword(SearchQueryScope.RECIPE, "kimchi", "invalid"))
                .isInstanceOf(SearchException.class)
                .extracting("error")
                .isEqualTo(SearchErrorCode.SEARCH_FAILED);
    }

    @Test
    @DisplayName("후보 검색에서 커서가 유효하지 않으면 SEARCH_FAILED를 던진다")
    void searchCandidatesWithPitInvalidCursor() throws CursorException {
        doThrow(new CursorException(com.cheftory.api._common.cursor.CursorErrorCode.INVALID_CURSOR))
                .when(scoreIdCursorCodec)
                .decode("invalid");

        assertThatThrownBy(() -> searchQueryService.searchCandidatesWithPit(
                        RankingSurfaceType.CUISINE_KOREAN,
                        RankingItemType.RECIPE,
                        10,
                        new PersonalizationProfile(List.of(), List.of()),
                        "pit-1",
                        "invalid"))
                .isInstanceOf(SearchException.class)
                .extracting("error")
                .isEqualTo(SearchErrorCode.SEARCH_FAILED);
    }
}
