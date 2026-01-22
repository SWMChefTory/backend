package com.cheftory.api.ranking.candidate;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.query.SearchPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingCandidateService {

    private final RankingCandidateSearchPort candidateSearchPort;

    public String openPit() {
        return candidateSearchPort.openPit();
    }

    public SearchPage searchWithPit(
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            int pageSize,
            PersonalizationProfile profile,
            String pitId,
            String searchAfter) {

        return candidateSearchPort.searchWithPit(surfaceType, itemType, pageSize, profile, pitId, searchAfter);
    }

    public void closePit(String pitId) {
        candidateSearchPort.closePit(pitId);
    }
}
