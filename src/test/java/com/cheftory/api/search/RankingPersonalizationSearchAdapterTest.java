package com.cheftory.api.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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
    @DisplayName("mgetSearchQueries delegates to service")
    void mgetSearchQueriesDelegatesToService() throws SearchException {
        List<String> ids = List.of("id-1", "id-2");
        List<SearchQuery> expected = List.of(SearchQuery.builder().id("id-1").build());

        doReturn(expected).when(searchQueryService).mgetSearchQueries(ids);

        List<SearchQuery> result = adapter.mgetSearchQueries(ids);

        assertThat(result).isEqualTo(expected);
        verify(searchQueryService).mgetSearchQueries(ids);
    }
}
