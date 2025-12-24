package com.cheftory.api.search.autocomplete;

import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
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

  public List<Autocomplete> searchAutocomplete(String keyword, Pageable pageable) {
    try {
      SearchRequest request = buildRequest(keyword, pageable);

      SearchResponse<Autocomplete> response = openSearchClient.search(request, Autocomplete.class);

      return response.hits().hits().stream().map(Hit::source).toList();

    } catch (Exception e) {
      log.error(SearchErrorCode.AUTOCOMPLETE_FAILED.getMessage(), keyword, e);
      throw new SearchException(SearchErrorCode.AUTOCOMPLETE_FAILED);
    }
  }

  private SearchRequest buildRequest(String keyword, Pageable pageable) {
    if (keyword == null || keyword.isBlank()) {
      return buildCountSortedRequest(pageable);
    }
    return buildAutocompleteRequest(keyword, pageable);
  }

  private SearchRequest buildAutocompleteRequest(String keyword, Pageable pageable) {
    return SearchRequest.of(
        s ->
            s.index("autocomplete")
                .query(buildFunctionScoreQuery(keyword))
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize()));
  }

  private SearchRequest buildCountSortedRequest(Pageable pageable) {
    return SearchRequest.of(
        s ->
            s.index("autocomplete")
                .query(q -> q.matchAll(m -> m))
                .sort(sort -> sort.field(f -> f.field("count").order(SortOrder.Desc)))
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize()));
  }

  private Query buildFunctionScoreQuery(String keyword) {
    return Query.of(
        q ->
            q.functionScore(
                fs ->
                    fs.query(buildMultiMatchQuery(keyword))
                        .functions(buildFieldValueFactorFunction())
                        .boostMode(FunctionBoostMode.Multiply)));
  }

  private Query buildMultiMatchQuery(String keyword) {
    return Query.of(
        q ->
            q.multiMatch(
                mm ->
                    mm.query(keyword)
                        .fields("text.ngram^5", "text.keyword^3", "text.icu^2", "text")
                        .type(TextQueryType.BestFields)
                        .fuzziness("AUTO")));
  }

  private List<FunctionScore> buildFieldValueFactorFunction() {
    return List.of(
        FunctionScore.of(
            f ->
                f.fieldValueFactor(
                    fv ->
                        fv.field("count")
                            .factor(1.2F)
                            .modifier(FieldValueFactorModifier.Log1p)
                            .missing(1.0))));
  }
}
