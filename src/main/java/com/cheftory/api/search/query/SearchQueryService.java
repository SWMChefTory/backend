package com.cheftory.api.search.query;

import com.cheftory.api.search.query.entity.SearchQuery;
import com.cheftory.api.search.utils.SearchPageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchQueryService {
  private final SearchQueryRepository searchQueryRepository;

  public Page<SearchQuery> searchByKeyword(String text, Integer page) {
    Pageable pageable = SearchPageRequest.create(page);
    return searchQueryRepository.searchByKeyword(text, pageable);
  }
}
