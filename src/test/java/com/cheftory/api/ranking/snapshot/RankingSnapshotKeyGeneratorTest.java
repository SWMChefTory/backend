package com.cheftory.api.ranking.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api._common.MarketContextTestExtension;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarketContextTestExtension.class)
@DisplayName("RankingSnapshotKeyGenerator")
class RankingSnapshotKeyGeneratorTest {

    @Test
    @DisplayName("pitKey should include market and request info")
    void pitKeyShouldIncludeMarketAndRequestInfo() throws CheftoryException {
        RankingSnapshotKeyGenerator generator = new RankingSnapshotKeyGenerator();
        UUID requestId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        String key = generator.pitKey(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);

        assertThat(key).isEqualTo("korea:ranking:request:" + requestId + ":CUISINE_KOREAN:RECIPE:pit");
    }

    @Test
    @DisplayName("impressionPosKey should include market and request info")
    void impressionPosKeyShouldIncludeMarketAndRequestInfo() throws CheftoryException {
        RankingSnapshotKeyGenerator generator = new RankingSnapshotKeyGenerator();
        UUID requestId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        String key = generator.impressionPosKey(requestId);

        assertThat(key).isEqualTo("korea:ranking:request:" + requestId + ":impressionPos");
    }
}
