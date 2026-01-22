package com.cheftory.api.ranking.interaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api._common.MarketContextTestExtension;
import com.cheftory.api.ranking.RankingItemType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarketContextTestExtension.class)
@DisplayName("RankingInteractionKeyGenerator Tests")
class RankingInteractionKeyGeneratorTest {

    private RankingInteractionKeyGenerator keyGenerator;

    @BeforeEach
    void setUp() {
        keyGenerator = new RankingInteractionKeyGenerator();
    }

    @Nested
    @DisplayName("recentViewsKey")
    class RecentViewsKey {

        @Test
        @DisplayName("should include market, item type, and user id")
        void shouldIncludeMarketItemTypeAndUserId() {
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            String key = keyGenerator.recentViewsKey(userId, RankingItemType.RECIPE);

            assertThat(key).isEqualTo("korea:ranking:RECIPE:user:123e4567-e89b-12d3-a456-426614174000:recentViews");
        }
    }

    @Nested
    @DisplayName("seenKey")
    class SeenKey {

        @Test
        @DisplayName("should include market, item type, and user id")
        void shouldIncludeMarketItemTypeAndUserId() {
            UUID userId = UUID.fromString("987fcdeb-51a2-43d1-b789-123456789abc");

            String key = keyGenerator.seenKey(userId, RankingItemType.RECIPE);

            assertThat(key).isEqualTo("korea:ranking:RECIPE:user:987fcdeb-51a2-43d1-b789-123456789abc:seen");
        }
    }
}
