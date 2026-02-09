package com.cheftory.api.ranking.candidate;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchPage;

public interface RankingCandidateSearchPort {

    String openPit() throws SearchException;

    SearchPage searchWithPit(
            RankingSurfaceType surfaceType,
            RankingItemType itemType,
            int size,
            PersonalizationProfile profile,
            String pitId,
            String cursor)
            throws SearchException;

    void closePit(String pitId);
}
