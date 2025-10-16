package com.cheftory.api.recipeinfo.search.autocomplete;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
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
public class RecipeAutocompleteRepository {

  private final OpenSearchClient openSearchClient;

  public List<RecipeAutocomplete> searchAutocomplete(String keyword, Pageable pageable) {
    try {
      SearchResponse<RecipeAutocomplete> response =
          openSearchClient.search(
              buildAutocompleteRequest(keyword, pageable), RecipeAutocomplete.class);

      return response.hits().hits().stream().map(Hit::source).toList();

    } catch (IOException e) {
      throw new RuntimeException("자동완성 검색 중 오류가 발생했습니다", e);
    }
  }

  private SearchRequest buildAutocompleteRequest(String keyword, Pageable pageable) {
    return SearchRequest.of(
        s ->
            s.index("autocomplete")
                .query(buildFunctionScoreQuery(keyword))
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
