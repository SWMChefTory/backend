package com.cheftory.api.search.autocomplete;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.FieldValueFactorModifier;
import org.opensearch.client.opensearch._types.query_dsl.FunctionBoostMode;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScore;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AutocompleteRepository {

  private final OpenSearchClient openSearchClient;

  private static final String INDEX = "autocomplete";
  private static final String FIELD_SCOPE = "scope";
  private static final String FIELD_MARKET = "market";
  private static final String FIELD_COUNT = "count";

  public List<Autocomplete> searchAutocomplete(
      AutocompleteScope scope, String keyword, Pageable pageable) {
    try {
      SearchRequest request = buildRequest(scope, keyword, pageable);
      SearchResponse<Autocomplete> response = openSearchClient.search(request, Autocomplete.class);

      return response.hits().hits().stream().map(Hit::source).toList();
    } catch (Exception e) {
      log.error(SearchErrorCode.AUTOCOMPLETE_FAILED.getMessage(), keyword, e);
      throw new SearchException(SearchErrorCode.AUTOCOMPLETE_FAILED);
    }
  }

  private SearchRequest buildRequest(AutocompleteScope scope, String keyword, Pageable pageable) {
    if (keyword == null || keyword.isBlank()) {
      return buildCountSortedRequest(scope, pageable);
    }
    return buildAutocompleteRequest(scope, keyword, pageable);
  }

  private SearchRequest buildAutocompleteRequest(
      AutocompleteScope scope, String keyword, Pageable pageable) {
    return SearchRequest.of(
        s ->
            s.index(INDEX)
                .query(buildFunctionScoreQuery(scope, keyword))
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize()));
  }

  private SearchRequest buildCountSortedRequest(AutocompleteScope scope, Pageable pageable) {
    return SearchRequest.of(
        s ->
            s.index(INDEX)
                .query(buildFilteredMatchAll(scope))
                .sort(sort -> sort.field(f -> f.field(FIELD_COUNT).order(SortOrder.Desc)))
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize()));
  }

  private Query buildFunctionScoreQuery(AutocompleteScope scope, String keyword) {
    return Query.of(
        q ->
            q.functionScore(
                fs ->
                    fs.query(buildFilteredMultiMatch(scope, keyword))
                        .functions(buildFieldValueFactorFunction())
                        .boostMode(FunctionBoostMode.Multiply)));
  }

  private Query buildFilteredMultiMatch(AutocompleteScope scope, String keyword) {
    String scopeValue = scope.name().toLowerCase();
    String marketValue = currentMarketKey();

    return Query.of(
        q ->
            q.bool(
                b ->
                    b.filter(
                            f -> f.term(t -> t.field(FIELD_SCOPE).value(FieldValue.of(scopeValue))))
                        .filter(
                            f ->
                                f.term(
                                    t -> t.field(FIELD_MARKET).value(FieldValue.of(marketValue))))
                        .must(
                            m ->
                                m.multiMatch(
                                    mm ->
                                        mm.query(keyword)
                                            .fields(
                                                "text.ngram^5",
                                                "text.keyword^3",
                                                "text.icu^2",
                                                "text")
                                            .type(TextQueryType.BestFields)
                                            .fuzziness("AUTO")))));
  }

  private Query buildFilteredMatchAll(AutocompleteScope scope) {
    String scopeValue = scope.name().toLowerCase();
    String marketValue = currentMarketKey();

    return Query.of(
        q ->
            q.bool(
                b ->
                    b.filter(
                            f -> f.term(t -> t.field(FIELD_SCOPE).value(FieldValue.of(scopeValue))))
                        .filter(
                            f ->
                                f.term(
                                    t -> t.field(FIELD_MARKET).value(FieldValue.of(marketValue))))
                        .must(m -> m.matchAll(ma -> ma))));
  }

  private String currentMarketKey() {
    Market market = MarketContext.required().market();
    return market.name().toLowerCase();
  }

  private List<FunctionScore> buildFieldValueFactorFunction() {
    return List.of(
        FunctionScore.of(
            f ->
                f.fieldValueFactor(
                    fv ->
                        fv.field(FIELD_COUNT)
                            .factor(1.2F)
                            .modifier(FieldValueFactorModifier.Log1p)
                            .missing(1.0))));
  }
}
