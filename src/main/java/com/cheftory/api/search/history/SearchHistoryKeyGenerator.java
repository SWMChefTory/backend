package com.cheftory.api.search.history;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SearchHistoryKeyGenerator {

  private static final String PREFIX = "searchHistory";
  private static final char SEP = ':';

  public String generate(UUID userId, SearchHistoryScope scope) {

    Market market = MarketContext.required().market();
    String marketKey = market.name().toLowerCase();

    return new StringBuilder()
        .append(marketKey)
        .append(SEP)
        .append(PREFIX)
        .append(SEP)
        .append(scope.name().toLowerCase())
        .append(SEP)
        .append(userId)
        .toString();
  }
}
