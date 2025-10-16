package com.cheftory.api.recipeinfo.search;

import com.cheftory.api.recipeinfo.search.exception.RecipeSearchErrorCode;
import com.cheftory.api.recipeinfo.search.exception.RecipeSearchException;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.FunctionBoostMode;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScore;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScoreMode;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecipeSearchRepository {

  private final OpenSearchClient openSearchClient;

  public Page<RecipeSearch> searchByKeyword(String keyword, Pageable pageable) {
    try {
      SearchResponse<RecipeSearch> response =
          openSearchClient.search(buildSearchRequest(keyword, pageable), RecipeSearch.class);

      List<RecipeSearch> content = response.hits().hits().stream().map(Hit::source).toList();

      long total = response.hits().total() != null ? response.hits().total().value() : 0;

      return new PageImpl<>(content, pageable, total);

    } catch (Exception e) {
      log.error(RecipeSearchErrorCode.RECIPE_SEARCH_FAILED.getMessage(), keyword, e);
      throw new RecipeSearchException(RecipeSearchErrorCode.RECIPE_SEARCH_FAILED);
    }
  }

  private SearchRequest buildSearchRequest(String keyword, Pageable pageable) {
    return SearchRequest.of(
        s ->
            s.index("recipes")
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
                        .functions(buildGaussDecayFunction())
                        .boostMode(FunctionBoostMode.Multiply)
                        .scoreMode(FunctionScoreMode.Sum)));
  }

  private Query buildMultiMatchQuery(String keyword) {
    return Query.of(
        q ->
            q.multiMatch(
                mm ->
                    mm.query(keyword)
                        .fields("searchText^10", "searchText.ngram")
                        .fuzziness("AUTO")
                        .minimumShouldMatch("50%")));
  }

  private List<FunctionScore> buildGaussDecayFunction() {
    return List.of(
        FunctionScore.of(
            f ->
                f.gauss(
                    g ->
                        g.field("created_at")
                            .placement(
                                p ->
                                    p.origin(JsonData.of("now"))
                                        .scale(JsonData.of("7d"))
                                        .decay(0.5)))));
  }
}
