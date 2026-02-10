package com.cheftory.api.ranking.personalization;

import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;

/**
 * 랭킹 개인화 검색 포트.
 *
 * <p>개인화 프로필 생성을 위한 검색 기능을 제공합니다.</p>
 */
public interface RankingPersonalizationSearchPort {
    /**
     * 검색 쿼리들을 일괄 조회합니다.
     *
     * @param ids 검색 쿼리 ID 목록
     * @return 검색 쿼리 목록
     * @throws SearchException 검색 예외
     */
    List<SearchQuery> mgetSearchQueries(List<String> ids) throws SearchException;
}
