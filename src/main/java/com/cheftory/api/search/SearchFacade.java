package com.cheftory.api.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.history.SearchHistoryService;
import com.cheftory.api.search.query.SearchQueryScope;
import com.cheftory.api.search.query.SearchQueryService;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 검색 파사드.
 *
 * <p>검색 관련 작업을 조율하는 퍼사드 클래스입니다.</p>
 */
@Service
@RequiredArgsConstructor
public class SearchFacade {

    /** 검색 쿼리 서비스. */
    private final SearchQueryService searchQueryService;

    /** 검색 히스토리 서비스. */
    private final SearchHistoryService searchHistoryService;

    /**
     * 검색을 수행합니다.
     *
     * <p>검색어를 검색 히스토리에 저장하고 검색을 수행합니다.</p>
     *
     * @param scope 검색 범위
     * @param userId 사용자 ID
     * @param keyword 검색어
     * @param cursor 커서
     * @return 검색 쿼리 커서 페이지
     * @throws SearchException 검색 예외
     */
    public CursorPage<SearchQuery> search(SearchQueryScope scope, UUID userId, String keyword, String cursor)
            throws SearchException {
        if (!keyword.isBlank()) searchHistoryService.create(userId, keyword);
        return searchQueryService.searchByKeyword(scope, keyword, cursor);
    }
}
