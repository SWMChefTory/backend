package com.cheftory.api.ranking.interaction;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingItemType;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * 랭킹 상호작용 키 생성기.
 *
 * <p>Redis 캐시 키를 생성하기 위한 유틸리티 클래스입니다.</p>
 */
@Component
public class RankingInteractionKeyGenerator {

    /** 키 구분자 */
    private static final String DELIMITER = ":";

    /** 랭킹 접두사 */
    private static final String PREFIX = "ranking";
    /** 사용자 */
    private static final String USER = "user";

    /** 최근 조회 */
    private static final String RECENT_VIEWS = "recentViews";
    /** 조회 완료 */
    private static final String SEEN = "seen";

    /**
     * 최근 조회 키를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @return 최근 조회 키
     * @throws CheftoryException 마켓 컨텍스트를 찾을 수 없는 경우
     */
    public String recentViewsKey(UUID userId, RankingItemType itemType) throws CheftoryException {
        return buildKey(PREFIX, itemType.name(), USER, userId.toString(), RECENT_VIEWS);
    }

    /**
     * 조회 완료 키를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param itemType 아이템 타입
     * @return 조회 완료 키
     * @throws CheftoryException 마켓 컨텍스트를 찾을 수 없는 경우
     */
    public String seenKey(UUID userId, RankingItemType itemType) throws CheftoryException {
        return buildKey(PREFIX, itemType.name(), USER, userId.toString(), SEEN);
    }

    /**
     * 키를 빌드합니다.
     *
     * @param parts 키 구성 요소들
     * @return 빌드된 키
     * @throws CheftoryException 마켓 컨텍스트를 찾을 수 없는 경우
     */
    private String buildKey(String... parts) throws CheftoryException {
        Market market = MarketContext.required().market();
        return market.name().toLowerCase() + DELIMITER + String.join(DELIMITER, parts);
    }
}
