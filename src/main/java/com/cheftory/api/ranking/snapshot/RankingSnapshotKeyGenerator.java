package com.cheftory.api.ranking.snapshot;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RankingSnapshotKeyGenerator {

    private static final String DELIMITER = ":";

    public String pitKey(UUID requestId, RankingSurfaceType surfaceType, RankingItemType itemType)
            throws CheftoryException {
        return buildKey("ranking", "request", requestId.toString(), surfaceType.name(), itemType.name(), "pit");
    }

    public String impressionPosKey(UUID requestId) throws CheftoryException {
        return buildKey("ranking", "request", requestId.toString(), "impressionPos");
    }

    private String buildKey(String... parts) throws CheftoryException {
        Market market = MarketContext.required().market();
        return new StringBuilder(market.name().toLowerCase())
                .append(DELIMITER)
                .append(String.join(DELIMITER, parts))
                .toString();
    }
}
