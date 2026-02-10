package com.cheftory.api.ranking.candidate;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 랭킹 후보 서비스.
 *
 * <p>OpenSearch Point In Time (PIT)을 사용하여 일관된 랭킹 후보 조회를 제공합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class RankingCandidateService {

    private final RankingCandidateSearchPort candidateSearchPort;

    /**
     * Point In Time (PIT)을 생성합니다.
     *
     * @return PIT ID
     * @throws SearchException 검색 예외
     */
    public String openPit() throws SearchException {
        return candidateSearchPort.openPit();
    }

    /**
     * PIT를 사용하여 검색을 수행합니다.
     *
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     * @param pageSize 페이지 크기
     * @param profile 개인화 프로필
     * @param pitId PIT ID
     * @param searchAfter 검색 후 커서
     * @return 검색 결과 페이지
     * @throws SearchException 검색 예외
     */
    public SearchPage searchWithPit(
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            int pageSize,
            PersonalizationProfile profile,
            String pitId,
            String searchAfter)
            throws SearchException {

        return candidateSearchPort.searchWithPit(surfaceType, itemType, pageSize, profile, pitId, searchAfter);
    }

    /**
     * PIT를 닫습니다.
     *
     * @param pitId PIT ID
     */
    public void closePit(String pitId) {
        candidateSearchPort.closePit(pitId);
    }
}
