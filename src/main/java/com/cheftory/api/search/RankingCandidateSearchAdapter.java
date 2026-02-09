package com.cheftory.api.search;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.candidate.RankingCandidateSearchPort;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchPage;
import com.cheftory.api.search.query.SearchQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingCandidateSearchAdapter implements RankingCandidateSearchPort {

    private final SearchQueryService searchQueryService;

    @Override
    public String openPit() throws SearchException {
        return searchQueryService.openPitForCandidates();
    }

    @Override
    public SearchPage searchWithPit(
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            int size,
            PersonalizationProfile profile,
            String pitId,
            String cursor)
            throws SearchException {
        return searchQueryService.searchCandidatesWithPit(surfaceType, itemType, size, profile, pitId, cursor);
    }

    @Override
    public void closePit(String pitId) {
        searchQueryService.closePit(pitId);
    }
}
