package com.cheftory.api.search.query;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.ScoreIdCursor;
import com.cheftory.api._common.cursor.ScoreIdCursorCodec;
import com.cheftory.api.search.query.entity.SearchQuery;
import com.cheftory.api.search.utils.SearchPageRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchQueryService {
  private final SearchQueryRepository searchQueryRepository;
  private final ScoreIdCursorCodec scoreIdCursorCodec;
  private static final int CURSOR_PAGE_SIZE = 20;

  @Deprecated(forRemoval = true)
  public Page<SearchQuery> searchByKeyword(SearchQueryScope scope, String text, int page) {
    Pageable pageable = SearchPageRequest.create(page);
    return searchQueryRepository.searchByKeyword(scope, text, pageable);
  }

  public CursorPage<SearchQuery> searchByKeyword(
      SearchQueryScope scope, String text, String cursor) {
    boolean first = (cursor == null || cursor.isBlank());
    Pageable pageable = PageRequest.of(0, CURSOR_PAGE_SIZE + 1);

    ScoreIdCursor scoreIdCursor = first ? null : scoreIdCursorCodec.decode(cursor);

    String anchorNowIso = first ? scoreIdCursorCodec.newAnchorNow() : scoreIdCursor.anchorNowIso();
    String pitId = first ? searchQueryRepository.createPitId() : scoreIdCursor.pitId();

    List<Hit<SearchQuery>> rows =
        first
            ? searchQueryRepository.searchByKeywordCursorFirst(
                scope, text, anchorNowIso, pitId, pageable)
            : searchQueryRepository.searchByKeywordCursorKeyset(
                scope,
                text,
                anchorNowIso,
                pitId,
                scoreIdCursor.score(),
                scoreIdCursor.id(),
                pageable);

    CursorPage<Hit<SearchQuery>> page =
        CursorPages.of(
            rows,
            CURSOR_PAGE_SIZE,
            last ->
                scoreIdCursorCodec.encode(
                    new ScoreIdCursor(score(last), last.id(), anchorNowIso, pitId)));

    List<SearchQuery> content = page.items().stream().map(Hit::source).toList();
    return CursorPage.of(content, page.nextCursor());
  }

  private static double score(Hit<SearchQuery> hit) {
    return hit.score() != null ? hit.score() : 0.0;
  }
}
