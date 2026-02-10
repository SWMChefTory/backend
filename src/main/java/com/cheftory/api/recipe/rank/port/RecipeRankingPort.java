package com.cheftory.api.recipe.rank.port;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingEventType;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.util.UUID;

/**
 * 레시피 랭킹 포트.
 *
 * <p>랭킹 이벤트 로깅 및 레시피 추천을 제공합니다.</p>
 */
public interface RecipeRankingPort {

    /**
     * 랭킹 이벤트 로깅.
     *
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @param itemId 아이템 ID
     * @param eventType 이벤트 타입
     * @param requestId 요청 ID
     * @throws CheftoryException 처리 실패 시
     */
    void logEvent(UUID userId, RankingItemType itemType, UUID itemId, RankingEventType eventType, UUID requestId)
            throws CheftoryException;

    /**
     * 레시피 추천 목록 조회.
     *
     * @param userId 사용자 ID
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     * @param cursor 페이징 커서
     * @param pageSize 페이지 크기
     * @return 레시피 ID 커서 페이지
     * @throws CheftoryException 처리 실패 시
     */
    CursorPage<UUID> recommend(
            UUID userId, RankingSurfaceType surfaceType, RankingItemType itemType, String cursor, int pageSize)
            throws CheftoryException;
}
