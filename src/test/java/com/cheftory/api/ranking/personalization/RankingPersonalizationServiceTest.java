package com.cheftory.api.ranking.personalization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RankingPersonalizationService Tests")
class RankingPersonalizationServiceTest {

    @Test
    @DisplayName("aggregateProfile aggregates keywords and channels")
    void aggregateProfileAggregatesKeywordsAndChannels() {
        RankingPersonalizationSearchPort searchPort = mock(RankingPersonalizationSearchPort.class);
        RankingPersonalizationService service = new RankingPersonalizationService(searchPort);

        SearchQuery query1 = SearchQuery.builder()
                .id("id-1")
                .keywords(List.of("kimchi", "soup"))
                .channelTitle("channel-a")
                .build();
        SearchQuery query2 = SearchQuery.builder()
                .id("id-2")
                .keywords(List.of("kimchi", "noodle"))
                .channelTitle("channel-b")
                .build();
        SearchQuery query3 = SearchQuery.builder()
                .id("id-3")
                .keywords(List.of("noodle"))
                .channelTitle("channel-a")
                .build();

        List<UUID> seedIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<String> expectedIds = List.of(seedIds.get(0).toString(), seedIds.get(1).toString());
        doReturn(List.of(query1, query2, query3))
                .when(searchPort)
                .mgetSearchQueries(expectedIds);

        PersonalizationProfile result = service.aggregateProfile(seedIds);

        assertThat(result.keywordsTop()).containsExactly("kimchi", "noodle", "soup");
        assertThat(result.channelsTop()).containsExactly("channel-a", "channel-b");
        verify(searchPort).mgetSearchQueries(expectedIds);
    }

    @Test
    @DisplayName("aggregateProfile throws when keywords are null")
    void aggregateProfileThrowsWhenKeywordsNull() {
        RankingPersonalizationSearchPort searchPort = mock(RankingPersonalizationSearchPort.class);
        RankingPersonalizationService service = new RankingPersonalizationService(searchPort);

        SearchQuery query = SearchQuery.builder().id("id-1").channelTitle("channel-a").build();
        doReturn(List.of(query)).when(searchPort).mgetSearchQueries(anyList());

        assertThatThrownBy(() -> service.aggregateProfile(List.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("seedDoc.keywords is null");
    }

    @Test
    @DisplayName("aggregateProfile throws when channelTitle is null")
    void aggregateProfileThrowsWhenChannelTitleNull() {
        RankingPersonalizationSearchPort searchPort = mock(RankingPersonalizationSearchPort.class);
        RankingPersonalizationService service = new RankingPersonalizationService(searchPort);

        SearchQuery query = SearchQuery.builder().id("id-1").keywords(List.of("kimchi")).build();
        doReturn(List.of(query)).when(searchPort).mgetSearchQueries(anyList());

        assertThatThrownBy(() -> service.aggregateProfile(List.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("seedDoc.channelTitle is null");
    }
}
