package com.cheftory.api.ranking.candidate;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchPage;

/**
 * 랭킹 후보 검색 포트.
 *
 * <p>OpenSearch를 사용하여 랭킹 후보를 검색하는 기능을 제공합니다.</p>
 */
public interface RankingCandidateSearchPort {

    /**
     * Point In Time (PIT)을 생성합니다.
     *
     * @return PIT ID
     * @throws SearchException 검색 예외
     */
    String openPit() throws SearchException;

    /**
     * PIT를 사용하여 랭킹 후보를 검색합니다.
     *
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     * @param size 페이지 크기
     * @param profile 개인화 프로필
     * @param pitId PIT ID
     * @param cursor 검색 커서
     * @return 검색 결과 페이지
     * @throws SearchException 검색 예외
     */
    SearchPage searchWithPit(
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            int size,
            PersonalizationProfile profile,
            String pitId,
            String cursor)
            throws SearchException;

    /**
     * PIT를 닫습니다.
     *
     * @param pitId PIT ID
     */
    void closePit(String pitId);
}
