package com.cheftory.api.search.query;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.CreatePitRequest;
import org.opensearch.client.opensearch.core.DeletePitRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.Pit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchQueryRepository {

    private static final String INDEX = "search_query";
    private static final Time PIT_KEEP_ALIVE = new Time.Builder().time("2m").build();

    private static final String FIELD_SCOPE = "scope";
    private static final String FIELD_MARKET = "market";
    private static final String FIELD_KEYWORDS = "keywords";
    private static final String FIELD_CHANNEL_TITLE = "channel_title";

    private static final String FIELD_CREATED_AT = "created_at";
    private static final String DECAY_SCALE = "7d";
    private static final double DECAY_VALUE = 0.5;

    private final OpenSearchClient openSearchClient;

    @Deprecated(forRemoval = true)
    public Page<SearchQuery> searchByKeyword(SearchQueryScope scope, String keyword, Pageable pageable) {

        try {
            SearchResponse<SearchQuery> response =
                    openSearchClient.search(pageRequest(scope, keyword, pageable), SearchQuery.class);

            List<SearchQuery> content =
                    response.hits().hits().stream().map(Hit::source).toList();
            long total =
                    response.hits().total() != null ? response.hits().total().value() : 0;
            return new PageImpl<>(content, pageable, total);

        } catch (Exception e) {
            log.error(SearchErrorCode.SEARCH_FAILED.getMessage(), keyword, e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED);
        }
    }

    public String createPitId() {
        try {
            return openSearchClient
                    .createPit(CreatePitRequest.of(p -> p.index(INDEX).keepAlive(PIT_KEEP_ALIVE)))
                    .pitId();
        } catch (Exception e) {
            log.error("create PIT failed", e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED);
        }
    }

    public void closePit(String pitId) {
        try {
            openSearchClient.deletePit(DeletePitRequest.of(c -> c.pitId(pitId)));
        } catch (Exception e) {
            log.warn("close PIT failed: pitId={}", pitId, e);
        }
    }

    public List<Hit<SearchQuery>> searchByKeywordCursorFirst(
            SearchQueryScope scope, String keyword, String anchorNowIso, String pitId, Pageable pageable) {
        Query q = buildFunctionScoreQuery(scope, keyword, anchorNowIso);
        return searchHits(cursorTemplateRequest(pitId, pageable, q, null, null), keyword);
    }

    public List<SearchQuery> mgetSearchQueries(List<String> ids) {
        try {
            var response = openSearchClient.mget(
                    mgetRequest -> mgetRequest
                            .index(INDEX)
                            .docs(ids.stream()
                                    .map(id -> org.opensearch.client.opensearch.core.mget.MultiGetOperation.of(
                                            op -> op.id(id)))
                                    .toList()),
                    SearchQuery.class);

            return response.docs().stream().map(item -> item.result().source()).toList();

        } catch (Exception e) {
            log.error("mget seeds failed: size={}", ids.size(), e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED);
        }
    }

    public List<Hit<SearchQuery>> searchByKeywordCursorKeyset(
            SearchQueryScope scope,
            String keyword,
            String anchorNowIso,
            String pitId,
            double lastScore,
            String lastId,
            Pageable pageable) {
        Query q = buildFunctionScoreQuery(scope, keyword, anchorNowIso);
        return searchHits(cursorTemplateRequest(pitId, pageable, q, lastScore, lastId), keyword);
    }

    public List<Hit<SearchQuery>> searchCandidatesCursorFirst(
            SearchQueryScope scope,
            String label,
            String anchorNowIso,
            String pitId,
            PersonalizationProfile profile,
            Pageable pageable) {
        Query q = buildCandidatesFunctionScoreQuery(scope, label, anchorNowIso, profile);
        return searchHits(cursorTemplateRequest(pitId, pageable, q, null, null), "candidates");
    }

    public List<Hit<SearchQuery>> searchCandidatesCursorKeyset(
            SearchQueryScope scope,
            String label,
            String anchorNowIso,
            String pitId,
            PersonalizationProfile profile,
            double lastScore,
            String lastId,
            Pageable pageable) {
        Query q = buildCandidatesFunctionScoreQuery(scope, label, anchorNowIso, profile);
        return searchHits(cursorTemplateRequest(pitId, pageable, q, lastScore, lastId), "candidates");
    }

    private SearchRequest pageRequest(SearchQueryScope scope, String keyword, Pageable pageable) {
        return SearchRequest.of(s -> s.index(INDEX)
                .query(buildFunctionScoreQuery(scope, keyword, "now"))
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize()));
    }

    private SearchRequest cursorTemplateRequest(
            String pitId, Pageable pageable, Query query, Double afterScore, String afterId) {
        return SearchRequest.of(s -> {
            s.pit(Pit.of(p -> p.id(pitId).keepAlive(PIT_KEEP_ALIVE.time())));
            s.size(pageable.getPageSize());
            s.trackScores(true);
            s.query(query);
            s.sort(so -> so.score(sc -> sc.order(SortOrder.Desc)));
            s.sort(so -> so.field(f -> f.field("_id").order(SortOrder.Asc)));
            applySearchAfter(s, afterScore, afterId);
            return s;
        });
    }

    private Query buildFunctionScoreQuery(SearchQueryScope scope, String keyword, String targetTime) {
        String scopeValue = scope.name().toLowerCase();
        String marketValue = currentMarketKey();

        Query baseQuery = Query.of(queryBuilder -> queryBuilder.bool(boolQuery -> {
            boolQuery.filter(
                    filter -> filter.term(term -> term.field(FIELD_MARKET).value(FieldValue.of(marketValue))));
            boolQuery.filter(
                    filter -> filter.term(term -> term.field(FIELD_SCOPE).value(FieldValue.of(scopeValue))));
            boolQuery.must(must -> must.multiMatch(multiMatch -> multiMatch
                    .query(keyword)
                    .fields("searchText^10", "searchText.ngram")
                    .fuzziness("AUTO")
                    .minimumShouldMatch("50%")));
            return boolQuery;
        }));

        List<FunctionScore> functions = List.of(FunctionScore.of(functionScore ->
                functionScore.gauss(gauss -> gauss.field(FIELD_CREATED_AT).placement(placement -> placement
                        .origin(JsonData.of(targetTime))
                        .scale(JsonData.of("7d"))
                        .decay(0.5)))));

        return Query.of(queryBuilder -> queryBuilder.functionScore(functionScore -> functionScore
                .query(baseQuery)
                .functions(functions)
                .boostMode(FunctionBoostMode.Sum)
                .scoreMode(FunctionScoreMode.Sum)));
    }

    private Query buildCandidatesFunctionScoreQuery(
            SearchQueryScope scope, String label, String targetTime, PersonalizationProfile profile) {

        String scopeValue = scope.name().toLowerCase();
        String marketValue = currentMarketKey();

        Query filterOnly = Query.of(qb -> qb.bool(b -> {
            b.filter(f -> f.term(t -> t.field(FIELD_MARKET).value(FieldValue.of(marketValue))));
            b.filter(f -> f.term(t -> t.field(FIELD_SCOPE).value(FieldValue.of(scopeValue))));
            b.filter(f -> f.term(t -> t.field(FIELD_KEYWORDS).value(FieldValue.of(label))));
            return b;
        }));

        List<FunctionScore> functions = new java.util.ArrayList<>();

        functions.add(FunctionScore.of(fs -> fs.filter(fq -> fq.terms(t -> t.field(FIELD_KEYWORDS)
                        .terms(tv -> tv.value(profile.keywordsTop().stream()
                                .map(FieldValue::of)
                                .toList()))))
                .weight(0.3f)));

        for (String channel : profile.channelsTop()) {
            functions.add(FunctionScore.of(fs -> fs.filter(fq -> fq.match(m -> m.field(FIELD_CHANNEL_TITLE)
                            .query(FieldValue.of(channel))
                            .operator(Operator.And)))
                    .weight(0.7f)));
        }

        functions.add(FunctionScore.of(
                fs -> fs.gauss(g -> g.field(FIELD_CREATED_AT).placement(p -> p.origin(JsonData.of(targetTime))
                                .scale(JsonData.of("60d"))
                                .decay(0.95)))
                        .weight(0.1f)));

        return Query.of(qb -> qb.functionScore(fs -> fs.query(filterOnly)
                .functions(functions)
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Sum)));
    }

    private void applySearchAfter(SearchRequest.Builder s, Double afterScore, String afterId) {
        if (afterScore == null || afterId == null || afterId.isBlank()) return;
        s.searchAfter(
                List.of(FieldValue.of(v -> v.doubleValue(afterScore)), FieldValue.of(v -> v.stringValue(afterId))));
    }

    private List<Hit<SearchQuery>> searchHits(SearchRequest request, String keywordForLog) {
        try {
            return openSearchClient.search(request, SearchQuery.class).hits().hits();
        } catch (Exception e) {
            log.error(SearchErrorCode.SEARCH_FAILED.getMessage(), keywordForLog, e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED);
        }
    }

    private String currentMarketKey() {
        Market market = MarketContext.required().market();
        return market.name().toLowerCase();
    }

    private List<FunctionScore> buildGaussDecayFunction(String targetTime, String scale, Double decay) {
        return List.of(FunctionScore.of(functionScore ->
                functionScore.gauss(gauss -> gauss.field(FIELD_CREATED_AT).placement(placement -> placement
                        .origin(JsonData.of(targetTime))
                        .scale(JsonData.of(scale))
                        .decay(decay)))));
    }
}
