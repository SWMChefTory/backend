package com.cheftory.api.search.query;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
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
import org.opensearch.client.opensearch._types.query_dsl.FunctionBoostMode;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScore;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScoreMode;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.CreatePitRequest;
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

  private final OpenSearchClient openSearchClient;

  @Deprecated(forRemoval = true)
  public Page<SearchQuery> searchByKeyword(
      SearchQueryScope scope, String keyword, Pageable pageable) {
    SearchResponse<SearchQuery> response =
        doSearchResponse(pageRequest(scope, keyword, pageable), keyword);

    List<SearchQuery> content = response.hits().hits().stream().map(Hit::source).toList();
    long total = response.hits().total() != null ? response.hits().total().value() : 0;
    return new PageImpl<>(content, pageable, total);
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

  public List<Hit<SearchQuery>> searchByKeywordCursorFirst(
      SearchQueryScope scope,
      String keyword,
      String anchorNowIso,
      String pitId,
      Pageable pageable) {
    return doSearch(
        cursorRequest(scope, keyword, anchorNowIso, pitId, null, null, pageable), keyword);
  }

  public List<Hit<SearchQuery>> searchByKeywordCursorKeyset(
      SearchQueryScope scope,
      String keyword,
      String anchorNowIso,
      String pitId,
      double lastScore,
      String lastId,
      Pageable pageable) {
    return doSearch(
        cursorRequest(scope, keyword, anchorNowIso, pitId, lastScore, lastId, pageable), keyword);
  }

  private SearchRequest pageRequest(SearchQueryScope scope, String keyword, Pageable pageable) {
    return SearchRequest.of(
        s ->
            s.index(INDEX)
                .query(buildFunctionScoreQuery(scope, keyword, "now"))
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize()));
  }

  private SearchRequest cursorRequest(
      SearchQueryScope scope,
      String keyword,
      String anchorNowIso,
      String pitId,
      Double afterScore,
      String afterId,
      Pageable pageable) {

    return SearchRequest.of(
        s -> {
          applyPit(s, pitId);
          s.size(pageable.getPageSize())
              .trackScores(true)
              .query(buildFunctionScoreQuery(scope, keyword, anchorNowIso))
              .sort(so -> so.score(sc -> sc.order(SortOrder.Desc)))
              .sort(so -> so.field(f -> f.field("_id").order(SortOrder.Asc)));

          applySearchAfter(s, afterScore, afterId);
          return s;
        });
  }

  private void applyPit(SearchRequest.Builder s, String pitId) {
    s.pit(Pit.of(p -> p.id(pitId).keepAlive(PIT_KEEP_ALIVE.time())));
  }

  private void applySearchAfter(SearchRequest.Builder s, Double afterScore, String afterId) {
    if (afterScore == null || afterId == null || afterId.isBlank()) return;
    s.searchAfter(
        List.of(
            FieldValue.of(v -> v.doubleValue(afterScore)),
            FieldValue.of(v -> v.stringValue(afterId))));
  }

  private List<Hit<SearchQuery>> doSearch(SearchRequest request, String keywordForLog) {
    return doSearchResponse(request, keywordForLog).hits().hits();
  }

  private SearchResponse<SearchQuery> doSearchResponse(
      SearchRequest request, String keywordForLog) {
    try {
      return openSearchClient.search(request, SearchQuery.class);
    } catch (Exception e) {
      log.error(SearchErrorCode.SEARCH_FAILED.getMessage(), keywordForLog, e);
      throw new SearchException(SearchErrorCode.SEARCH_FAILED);
    }
  }

  private Query buildFunctionScoreQuery(SearchQueryScope scope, String keyword, String targetTime) {
    String scopeValue = scope.name().toLowerCase();
    String marketValue = currentMarketKey();

    return Query.of(
        q ->
            q.functionScore(
                fs ->
                    fs.query(buildFilteredMultiMatch(marketValue, scopeValue, keyword))
                        .functions(buildGaussDecayFunction(targetTime))
                        .boostMode(FunctionBoostMode.Multiply)
                        .scoreMode(FunctionScoreMode.Sum)));
  }

  private Query buildFilteredMultiMatch(String marketValue, String scopeValue, String keyword) {
    return Query.of(
        q ->
            q.bool(
                b ->
                    b.filter(
                            f ->
                                f.term(
                                    t -> t.field(FIELD_MARKET).value(FieldValue.of(marketValue))))
                        .filter(
                            f -> f.term(t -> t.field(FIELD_SCOPE).value(FieldValue.of(scopeValue))))
                        .must(
                            m ->
                                m.multiMatch(
                                    mm ->
                                        mm.query(keyword)
                                            .fields("searchText^10", "searchText.ngram")
                                            .fuzziness("AUTO")
                                            .minimumShouldMatch("50%")))));
  }

  private String currentMarketKey() {
    Market market = MarketContext.required().market();
    return market.name().toLowerCase();
  }

  private List<FunctionScore> buildGaussDecayFunction(String targetTime) {
    return List.of(
        FunctionScore.of(
            f ->
                f.gauss(
                    g ->
                        g.field("created_at")
                            .placement(
                                p ->
                                    p.origin(JsonData.of(targetTime))
                                        .scale(JsonData.of("7d"))
                                        .decay(0.5)))));
  }
}
