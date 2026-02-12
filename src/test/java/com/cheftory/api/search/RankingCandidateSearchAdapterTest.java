package com.cheftory.api.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.candidate.RankingCandidateErrorCode;
import com.cheftory.api.ranking.candidate.RankingCandidateException;
import com.cheftory.api.ranking.candidate.RankingCandidatePage;
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
    void openPitDelegatesToService() throws Exception {
        doReturn("pit-1").when(searchQueryService).openPitForCandidates();

        String result = adapter.openPit();

        assertThat(result).isEqualTo("pit-1");
        verify(searchQueryService).openPitForCandidates();
    }

    @Test
    @DisplayName("searchWithPit delegates to service")
    void searchWithPitDelegatesToService() throws Exception {
        RankingSurfaceType surfaceType = RankingSurfaceType.CUISINE_KOREAN;
        RankingItemType itemType = RankingItemType.RECIPE;
        PersonalizationProfile profile = new PersonalizationProfile(List.of("kimchi"), List.of("channel"));
        SearchPage searchPage = new SearchPage(List.of(UUID.randomUUID()), "cursor");
        RankingCandidatePage expected = new RankingCandidatePage(searchPage.items(), searchPage.nextCursor());

        doReturn(searchPage)
                .when(searchQueryService)
                .searchCandidatesWithPit(surfaceType, itemType, 20, profile, "pit-1", "cursor");

        RankingCandidatePage result = adapter.searchWithPit(surfaceType, itemType, 20, profile, "pit-1", "cursor");

        assertThat(result).isEqualTo(expected);
        verify(searchQueryService).searchCandidatesWithPit(surfaceType, itemType, 20, profile, "pit-1", "cursor");
    }

    @Test
    @DisplayName("openPit converts SearchException to RankingCandidateException")
    void openPitConvertsException() throws Exception {
        doThrow(new SearchException(com.cheftory.api.search.exception.SearchErrorCode.SEARCH_FAILED))
                .when(searchQueryService)
                .openPitForCandidates();

        assertThatThrownBy(() -> adapter.openPit())
                .isInstanceOf(RankingCandidateException.class)
                .extracting("error")
                .isEqualTo(RankingCandidateErrorCode.RANKING_CANDIDATE_OPEN_FAILED);
    }

    @Test
    @DisplayName("searchWithPit converts SearchException to RankingCandidateException")
    void searchWithPitConvertsException() throws Exception {
        RankingSurfaceType surfaceType = RankingSurfaceType.CUISINE_KOREAN;
        RankingItemType itemType = RankingItemType.RECIPE;
        PersonalizationProfile profile = new PersonalizationProfile(List.of("kimchi"), List.of("channel"));
        doThrow(new SearchException(com.cheftory.api.search.exception.SearchErrorCode.SEARCH_FAILED))
                .when(searchQueryService)
                .searchCandidatesWithPit(surfaceType, itemType, 20, profile, "pit-1", "cursor");

        assertThatThrownBy(() -> adapter.searchWithPit(surfaceType, itemType, 20, profile, "pit-1", "cursor"))
                .isInstanceOf(RankingCandidateException.class)
                .extracting("error")
                .isEqualTo(RankingCandidateErrorCode.RANKING_CANDIDATE_SEARCH_FAILED);
    }

    @Test
    @DisplayName("closePit delegates to service")
    void closePitDelegatesToService() {
        adapter.closePit("pit-1");

        verify(searchQueryService).closePit("pit-1");
    }
}
