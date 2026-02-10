package com.cheftory.api.search;

import com.cheftory.api.ranking.personalization.RankingPersonalizationSearchPort;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchQueryService;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 랭킹 개인화 검색 어댑터.
 *
 * <p>검색 쿼리 서비스를 사용하여 랭킹 개인화 검색 포트를 구현합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class RankingPersonalizationSearchAdapter implements RankingPersonalizationSearchPort {

    /** 검색 쿼리 서비스. */
    private final SearchQueryService searchQueryService;

    /**
     * 여러 검색 쿼리를 조회합니다.
     *
     * @param ids 문서 ID 목록
     * @return 검색 쿼리 목록
     * @throws SearchException 검색 예외
     */
    @Override
    public List<SearchQuery> mgetSearchQueries(List<String> ids) throws SearchException {
        return searchQueryService.mgetSearchQueries(ids);
    }
}
