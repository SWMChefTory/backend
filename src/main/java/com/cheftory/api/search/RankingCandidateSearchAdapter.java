package com.cheftory.api.search;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.candidate.RankingCandidateSearchPort;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchPage;
import com.cheftory.api.search.query.SearchQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 랭킹 후보군 검색 어댑터.
 *
 * <p>검색 쿼리 서비스를 사용하여 랭킹 후보군 검색 포트를 구현합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class RankingCandidateSearchAdapter implements RankingCandidateSearchPort {

    /** 검색 쿼리 서비스. */
    private final SearchQueryService searchQueryService;

    /**
     * PIT를 엽니다.
     *
     * @return PIT ID
     * @throws SearchException 검색 예외
     */
    @Override
    public String openPit() throws SearchException {
        return searchQueryService.openPitForCandidates();
    }

    /**
     * PIT를 사용하여 검색합니다.
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
    @Override
    public SearchPage searchWithPit(
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            int size,
            PersonalizationProfile profile,
            String pitId,
            String cursor)
            throws SearchException {
        return searchQueryService.searchCandidatesWithPit(surfaceType, itemType, size, profile, pitId, cursor);
    }

    /**
     * PIT를 닫습니다.
     *
     * @param pitId PIT ID
     */
    @Override
    public void closePit(String pitId) {
        searchQueryService.closePit(pitId);
    }
}
