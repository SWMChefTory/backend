package com.cheftory.api.recipe.rank;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.exception.CheftoryException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class RankingKeyGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DELIMITER = ":";

    public String generateKey(RankingType type) {
        return buildKey(getTypePrefix(type), "ranking", getCurrentTimestamp());
    }

    public String getLatestKey(RankingType type) {
        return buildKey(getTypePrefix(type), "latest");
    }

    private String buildKey(String... parts) {
        Market market = MarketContext.required().market();

        return market.name().toLowerCase() + DELIMITER + String.join(DELIMITER, parts);
    }

    private String getTypePrefix(RankingType type) {
        return switch (type) {
            case TRENDING -> "trendRecipe";
            case CHEF -> "chefRecipe";
        };
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
