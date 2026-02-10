package com.cheftory.api.ranking;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.recipe.rank.port.RecipeRankingPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 레시피 랭킹 어댑터.
 *
 * <p>RecipeRankingPort 인터페이스를 구현하여 랭킹 서비스를 제공합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class RecipeRankingAdapter implements RecipeRankingPort {

    /** 랭킹 서비스 */
    private final RankingService rankingService;

    /**
     * 랭킹 이벤트를 기록합니다.
     *
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @param itemId 아이템 ID
     * @param eventType 이벤트 타입
     * @param requestId 요청 ID
     * @throws CheftoryException Cheftory 예외
     */
    @Override
    public void logEvent(UUID userId, RankingItemType itemType, UUID itemId, RankingEventType eventType, UUID requestId)
            throws CheftoryException {
        rankingService.event(userId, itemType, itemId, eventType, requestId);
    }

    /**
     * 개인화된 랭킹 추천을 반환합니다.
     *
     * @param userId 사용자 ID
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     * @param cursor 커서
     * @param pageSize 페이지 크기
     * @return 커서 페이지
     * @throws CheftoryException Cheftory 예외
     */
    @Override
    public CursorPage<UUID> recommend(
            UUID userId, RankingSurfaceType surfaceType, RankingItemType itemType, String cursor, int pageSize)
            throws CheftoryException {
        return rankingService.recommend(userId, surfaceType, itemType, cursor, pageSize);
    }
}
