package com.cheftory.api.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchPage;
import com.cheftory.api.search.query.SearchQueryService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingCandidateSearchAdapter Tests")
class RankingCandidateSearchAdapterTest {

    @Mock
    private SearchQueryService searchQueryService;

    @InjectMocks
    private RankingCandidateSearchAdapter adapter;

    @Test
    @DisplayName("openPit delegates to service")
    void openPitDelegatesToService() throws SearchException {
        doReturn("pit-1").when(searchQueryService).openPitForCandidates();

        String result = adapter.openPit();

        assertThat(result).isEqualTo("pit-1");
        verify(searchQueryService).openPitForCandidates();
    }

    @Test
    @DisplayName("searchWithPit delegates to service")
    void searchWithPitDelegatesToService() throws SearchException {
        RankingSurfaceType surfaceType = RankingSurfaceType.CUISINE_KOREAN;
        RankingItemType itemType = RankingItemType.RECIPE;
        PersonalizationProfile profile = new PersonalizationProfile(List.of("kimchi"), List.of("channel"));
        SearchPage expected = new SearchPage(List.of(UUID.randomUUID()), "cursor");

        doReturn(expected)
                .when(searchQueryService)
                .searchCandidatesWithPit(surfaceType, itemType, 20, profile, "pit-1", "cursor");

        SearchPage result = adapter.searchWithPit(surfaceType, itemType, 20, profile, "pit-1", "cursor");

        assertThat(result).isEqualTo(expected);
        verify(searchQueryService).searchCandidatesWithPit(surfaceType, itemType, 20, profile, "pit-1", "cursor");
    }

    @Test
    @DisplayName("closePit delegates to service")
    void closePitDelegatesToService() {
        adapter.closePit("pit-1");

        verify(searchQueryService).closePit("pit-1");
    }
}
