package com.cheftory.api.search;

import com.cheftory.api.ranking.personalization.RankingPersonalizationSearchPort;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchQueryService;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingPersonalizationSearchAdapter implements RankingPersonalizationSearchPort {

    private final SearchQueryService searchQueryService;

    @Override
    public List<SearchQuery> mgetSearchQueries(List<String> ids) throws SearchException {
        return searchQueryService.mgetSearchQueries(ids);
    }
}
