package com.cheftory.api.search.history;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import java.util.UUID;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/**
 * 검색 히스토리 키 생성기.
 *
 * <p>Redis에 저장할 검색 히스토리 키를 생성합니다.</p>
 */
@Component
public class SearchHistoryKeyGenerator {

    /** 키 접두사. */
    private static final String PREFIX = "searchHistory";

    /** 키 구분자. */
    private static final char SEP = ':';

    /**
     * 검색 히스토리 키를 생성합니다.
     *
     * <p>키 형식: {market}:searchHistory:{scope}:{userId}</p>
     *
     * @param userId 사용자 ID
     * @param scope 검색 범위
     * @return 생성된 키
     */
    @SneakyThrows
    public String generate(UUID userId, SearchHistoryScope scope) {

        Market market = MarketContext.required().market();
        String marketKey = market.name().toLowerCase();

        return marketKey + SEP + PREFIX + SEP + scope.name().toLowerCase() + SEP + userId;
    }
}
