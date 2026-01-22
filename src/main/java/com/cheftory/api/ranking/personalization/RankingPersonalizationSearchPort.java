package com.cheftory.api.ranking.personalization;

import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;

public interface RankingPersonalizationSearchPort {
    List<SearchQuery> mgetSearchQueries(List<String> ids);
}
