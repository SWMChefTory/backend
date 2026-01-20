package com.cheftory.api.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.search.history.SearchHistoryService;
import com.cheftory.api.search.query.SearchQueryScope;
import com.cheftory.api.search.query.SearchQueryService;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFacade {
  private final SearchQueryService searchQueryService;
  private final SearchHistoryService searchHistoryService;

  @Deprecated(forRemoval = true)
  public Page<SearchQuery> search(SearchQueryScope scope, UUID userId, String keyword, int page) {
    if (!keyword.isBlank()) searchHistoryService.create(userId, keyword);
    return searchQueryService.searchByKeyword(scope, keyword, page);
  }

  public CursorPage<SearchQuery> search(
      SearchQueryScope scope, UUID userId, String keyword, String cursor) {
    if (!keyword.isBlank()) searchHistoryService.create(userId, keyword);
    return searchQueryService.searchByKeyword(scope, keyword, cursor);
  }
}
