package com.cheftory.api.ranking.personalization;

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
     * @return 개인화 집계용 시드 문서 목록
     * @throws RankingPersonalizationException 처리 예외
     */
    List<RankingPersonalizationSeed> mgetSeeds(List<String> ids) throws RankingPersonalizationException;
}
