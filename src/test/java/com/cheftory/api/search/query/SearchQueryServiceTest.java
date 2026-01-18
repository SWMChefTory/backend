package com.cheftory.api.search.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.ScoreIdCursor;
import com.cheftory.api._common.cursor.ScoreIdCursorCodec;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
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

  @Mock private SearchQueryRepository searchQueryRepository;
  @Mock private ScoreIdCursorCodec scoreIdCursorCodec;
  @InjectMocks private SearchQueryService searchQueryService;

  @Test
  @DisplayName("cursor가 없으면 첫 페이지를 조회한다")
  void shouldSearchFirstPageWithCursor() {
    String keyword = "김치찌개";
    String anchorNow = "2024-01-01T10:00:00";
    String pitId = "pit-1";

    doReturn(anchorNow).when(scoreIdCursorCodec).newAnchorNow();
    doReturn(pitId).when(searchQueryRepository).createPitId();

    List<Hit<Object>> rows =
        IntStream.range(0, 21)
            .mapToObj(
                i ->
                    Hit.of(
                        h ->
                            h.id("id-" + i)
                                .score(1.0)
                                .source(SearchQuery.builder().id("id-" + i).build())))
            .toList();

    doReturn(rows)
        .when(searchQueryRepository)
        .searchByKeywordCursorFirst(eq(keyword), eq(anchorNow), eq(pitId), any(Pageable.class));
    doReturn("next-cursor").when(scoreIdCursorCodec).encode(any(ScoreIdCursor.class));

    CursorPage<SearchQuery> result = searchQueryService.searchByKeyword(keyword, null);

    assertThat(result.items()).hasSize(20);
    assertThat(result.nextCursor()).isEqualTo("next-cursor");
    verify(searchQueryRepository)
        .searchByKeywordCursorFirst(eq(keyword), eq(anchorNow), eq(pitId), any(Pageable.class));
  }

  @Test
  @DisplayName("cursor가 있으면 keyset을 조회한다")
  void shouldSearchWithCursorKeyset() {
    String keyword = "김치찌개";
    ScoreIdCursor decoded = new ScoreIdCursor(1.2, "id-10", "now", "pit");

    doReturn(decoded).when(scoreIdCursorCodec).decode("cursor");
    doReturn(List.of(Hit.of(h -> h.id("id-11").source(SearchQuery.builder().id("id-11").build()))))
        .when(searchQueryRepository)
        .searchByKeywordCursorKeyset(
            eq(keyword),
            eq(decoded.anchorNowIso()),
            eq(decoded.pitId()),
            eq(decoded.score()),
            eq(decoded.id()),
            any(Pageable.class));

    CursorPage<SearchQuery> result = searchQueryService.searchByKeyword(keyword, "cursor");

    assertThat(result.items()).hasSize(1);
    verify(searchQueryRepository)
        .searchByKeywordCursorKeyset(
            eq(keyword),
            eq(decoded.anchorNowIso()),
            eq(decoded.pitId()),
            eq(decoded.score()),
            eq(decoded.id()),
            any(Pageable.class));
  }
}
