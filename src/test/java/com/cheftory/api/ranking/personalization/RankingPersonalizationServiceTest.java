package com.cheftory.api.ranking.personalization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RankingPersonalizationService Tests")
class RankingPersonalizationServiceTest {

    @Test
    @DisplayName("aggregateProfile aggregates keywords and channels")
    void aggregateProfileAggregatesKeywordsAndChannels() throws Exception {
        RankingPersonalizationSearchPort searchPort = mock(RankingPersonalizationSearchPort.class);
        RankingPersonalizationService service = new RankingPersonalizationService(searchPort);

        RankingPersonalizationSeed query1 = new RankingPersonalizationSeed(List.of("kimchi", "soup"), "channel-a");
        RankingPersonalizationSeed query2 = new RankingPersonalizationSeed(List.of("kimchi", "noodle"), "channel-b");
        RankingPersonalizationSeed query3 = new RankingPersonalizationSeed(List.of("noodle"), "channel-a");

        List<UUID> seedIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<String> expectedIds =
                List.of(seedIds.get(0).toString(), seedIds.get(1).toString());
        doReturn(List.of(query1, query2, query3)).when(searchPort).mgetSeeds(expectedIds);

        PersonalizationProfile result = service.aggregateProfile(seedIds);

        assertThat(result.keywordsTop()).containsExactly("kimchi", "noodle", "soup");
        assertThat(result.channelsTop()).containsExactly("channel-a", "channel-b");
        verify(searchPort).mgetSeeds(expectedIds);
    }

    @Test
    @DisplayName("aggregateProfile throws when keywords are null")
    void aggregateProfileThrowsWhenKeywordsNull() throws Exception {
        RankingPersonalizationSearchPort searchPort = mock(RankingPersonalizationSearchPort.class);
        RankingPersonalizationService service = new RankingPersonalizationService(searchPort);

        RankingPersonalizationSeed query = new RankingPersonalizationSeed(null, "channel-a");
        doReturn(List.of(query)).when(searchPort).mgetSeeds(anyList());

        assertThatThrownBy(() ->
                        service.aggregateProfile(List.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("seedDoc.keywords is null");
    }

    @Test
    @DisplayName("aggregateProfile throws when channelTitle is null")
    void aggregateProfileThrowsWhenChannelTitleNull() throws Exception {
        RankingPersonalizationSearchPort searchPort = mock(RankingPersonalizationSearchPort.class);
        RankingPersonalizationService service = new RankingPersonalizationService(searchPort);

        RankingPersonalizationSeed query = new RankingPersonalizationSeed(List.of("kimchi"), null);
        doReturn(List.of(query)).when(searchPort).mgetSeeds(anyList());

        assertThatThrownBy(() ->
                        service.aggregateProfile(List.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("seedDoc.channelTitle is null");
    }
}
