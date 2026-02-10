package com.cheftory.api.ranking.snapshot;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * 랭킹 스냅샷 키 생성기.
 *
 * <p>Redis 캐시 키를 생성하기 위한 유틸리티 클래스입니다.</p>
 */
@Component
public class RankingSnapshotKeyGenerator {

    /** 키 구분자 */
    private static final String DELIMITER = ":";

    /**
     * PIT 키를 생성합니다.
     *
     * @param requestId 요청 ID
     * @param surfaceType 서피스 타입
     * @param itemType 아이템 타입
     * @return PIT 키
     */
    public String pitKey(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType) {
        return buildKey("ranking", "request", requestId.toString(), surfaceType.name(), itemType.name(), "pit");
    }

    /**
     * 노출 위치 키를 생성합니다.
     *
     * @param requestId 요청 ID
     * @return 노출 위치 키
     */
    public String impressionPosKey(UUID requestId) {
        return buildKey("ranking", "request", requestId.toString(), "impressionPos");
    }

    /**
     * 키를 빌드합니다.
     *
     * @param parts 키 구성 요소들
     * @return 빌드된 키
     */
    private String buildKey(String... parts) {
        Market market = MarketContext.required().market();
        return market.name().toLowerCase() + DELIMITER + String.join(DELIMITER, parts);
    }
}
