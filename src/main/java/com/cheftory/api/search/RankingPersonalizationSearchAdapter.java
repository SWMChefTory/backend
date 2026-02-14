package com.cheftory.api.search;

import com.cheftory.api.ranking.personalization.RankingPersonalizationErrorCode;
import com.cheftory.api.ranking.personalization.RankingPersonalizationException;
import com.cheftory.api.ranking.personalization.RankingPersonalizationSearchPort;
import com.cheftory.api.ranking.personalization.RankingPersonalizationSeed;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchQueryService;
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
     * @return 개인화 집계용 시드 문서 목록
     * @throws RankingPersonalizationException 처리 예외
     */
    @Override
    public List<RankingPersonalizationSeed> mgetSeeds(List<String> ids) throws RankingPersonalizationException {
        try {
            return searchQueryService.mgetSearchQueries(ids).stream()
                    .map(doc -> new RankingPersonalizationSeed(doc.getKeywords(), doc.getChannelTitle()))
                    .toList();
        } catch (SearchException exception) {
            throw new RankingPersonalizationException(
                    RankingPersonalizationErrorCode.RANKING_PERSONALIZATION_SEARCH_FAILED, exception);
        }
    }
}
