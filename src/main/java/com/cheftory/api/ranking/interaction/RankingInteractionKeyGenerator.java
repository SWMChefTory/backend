package com.cheftory.api.ranking.interaction;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingItemType;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RankingInteractionKeyGenerator {

    private static final String DELIMITER = ":";

    private static final String PREFIX = "ranking";
    private static final String USER = "user";

    private static final String RECENT_VIEWS = "recentViews";
    private static final String SEEN = "seen";

    public String recentViewsKey(UUID userId, RankingItemType itemType) throws CheftoryException {
        return buildKey(PREFIX, itemType.name(), USER, userId.toString(), RECENT_VIEWS);
    }

    public String seenKey(UUID userId, RankingItemType itemType) throws CheftoryException {
        return buildKey(PREFIX, itemType.name(), USER, userId.toString(), SEEN);
    }

    private String buildKey(String... parts) throws CheftoryException {
        Market market = MarketContext.required().market();
        return new StringBuilder(market.name().toLowerCase())
                .append(DELIMITER)
                .append(String.join(DELIMITER, parts))
                .toString();
    }
}
