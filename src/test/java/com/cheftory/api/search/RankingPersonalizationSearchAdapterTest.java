package com.cheftory.api.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.cheftory.api.ranking.personalization.RankingPersonalizationErrorCode;
import com.cheftory.api.ranking.personalization.RankingPersonalizationException;
import com.cheftory.api.ranking.personalization.RankingPersonalizationSeed;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchQueryService;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingPersonalizationSearchAdapter Tests")
class RankingPersonalizationSearchAdapterTest {

    @Mock
    private SearchQueryService searchQueryService;

    @InjectMocks
    private RankingPersonalizationSearchAdapter adapter;

    @Test
    @DisplayName("mgetSeeds maps search docs to seeds")
    void mgetSeedsMapsSearchDocsToSeeds() throws Exception {
        List<String> ids = List.of("id-1", "id-2");
        List<SearchQuery> searchQueries = List.of(SearchQuery.builder()
                .id("id-1")
                .keywords(List.of("kimchi"))
                .channelTitle("channel-a")
                .build());

        doReturn(searchQueries).when(searchQueryService).mgetSearchQueries(ids);

        List<RankingPersonalizationSeed> result = adapter.mgetSeeds(ids);

        assertThat(result).containsExactly(new RankingPersonalizationSeed(List.of("kimchi"), "channel-a"));
        verify(searchQueryService).mgetSearchQueries(ids);
    }

    @Test
    @DisplayName("mgetSeeds converts SearchException to RankingPersonalizationException")
    void mgetSeedsConvertsException() throws Exception {
        List<String> ids = List.of("id-1");
        doThrow(new SearchException(SearchErrorCode.SEARCH_FAILED))
                .when(searchQueryService)
                .mgetSearchQueries(ids);

        assertThatThrownBy(() -> adapter.mgetSeeds(ids))
                .isInstanceOf(RankingPersonalizationException.class)
                .extracting("error")
                .isEqualTo(RankingPersonalizationErrorCode.RANKING_PERSONALIZATION_SEARCH_FAILED);
    }
}
