package com.cheftory.api.search.autocomplete;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import org.springframework.stereotype.Repository;

/**
 * 자동완성 OpenSearch 리포지토리.
 *
 * <p>OpenSearch를 사용하여 자동완성 검색을 제공합니다.</p>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class AutocompleteRepository {

    /** OpenSearch 클라이언트. */
    private final OpenSearchClient openSearchClient;

    /** 인덱스 이름. */
    private static final String INDEX = "autocomplete";

    /** 필드: 범위. */
    private static final String FIELD_SCOPE = "scope";

    /** 필드: 마켓. */
    private static final String FIELD_MARKET = "market";

    /** 필드: 검색 횟수. */
    private static final String FIELD_COUNT = "count";

    /**
     * 자동완성 검색을 수행합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @param limit 최대 개수
     * @return 자동완성 목록
     * @throws SearchException 검색 예외
     */
    public List<Autocomplete> searchAutocomplete(AutocompleteScope scope, String keyword, int limit)
            throws SearchException {
        try {
            SearchRequest request = buildRequest(scope, keyword, limit);
            SearchResponse<Autocomplete> response = openSearchClient.search(request, Autocomplete.class);

            return response.hits().hits().stream().map(Hit::source).toList();
        } catch (Exception e) {
            log.error(SearchErrorCode.AUTOCOMPLETE_FAILED.getMessage(), keyword, e);
            throw new SearchException(SearchErrorCode.AUTOCOMPLETE_FAILED, e);
        }
    }

    /**
     * 검색 요청을 빌드합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @param limit 최대 개수
     * @return 검색 요청
     */
    private SearchRequest buildRequest(AutocompleteScope scope, String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return buildCountSortedRequest(scope, limit);
        }
        return buildAutocompleteRequest(scope, keyword, limit);
    }

    /**
     * 자동완성 검색 요청을 빌드합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @param limit 최대 개수
     * @return 검색 요청
     */
    private SearchRequest buildAutocompleteRequest(AutocompleteScope scope, String keyword, int limit) {
        return SearchRequest.of(s -> s.index(INDEX)
                .query(buildFunctionScoreQuery(scope, keyword))
                .from(0)
                .size(limit));
    }

    /**
     * 검색 횟수 기반 정렬 요청을 빌드합니다.
     *
     * @param scope 검색 범위
     * @param limit 최대 개수
     * @return 검색 요청
     */
    private SearchRequest buildCountSortedRequest(AutocompleteScope scope, int limit) {
        return SearchRequest.of(s -> s.index(INDEX)
                .query(buildFilteredMatchAll(scope))
                .sort(sort -> sort.field(f -> f.field(FIELD_COUNT).order(SortOrder.Desc)))
                .from(0)
                .size(limit));
    }

    /**
     * 함수 점수 쿼리를 빌드합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @return 함수 점수 쿼리
     */
    private Query buildFunctionScoreQuery(AutocompleteScope scope, String keyword) {
        return Query.of(q -> q.functionScore(fs -> fs.query(buildFilteredMultiMatch(scope, keyword))
                .functions(buildFieldValueFactorFunction())
                .boostMode(FunctionBoostMode.Multiply)));
    }

    /**
     * 필터링된 멀티 매치 쿼리를 빌드합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @return 쿼리
     */
    private Query buildFilteredMultiMatch(AutocompleteScope scope, String keyword) {
        String scopeValue = scope.name().toLowerCase();
        String marketValue = currentMarketKey();

        return Query.of(
                q -> q.bool(b -> b.filter(f -> f.term(t -> t.field(FIELD_SCOPE).value(FieldValue.of(scopeValue))))
                        .filter(f -> f.term(t -> t.field(FIELD_MARKET).value(FieldValue.of(marketValue))))
                        .must(m -> m.multiMatch(mm -> mm.query(keyword)
                                .fields("text.ngram^5", "text.keyword^3", "text.icu^2", "text")
                                .type(TextQueryType.BestFields)
                                .fuzziness("AUTO")))));
    }

    /**
     * 필터링된 MatchAll 쿼리를 빌드합니다.
     *
     * @param scope 검색 범위
     * @return 쿼리
     */
    private Query buildFilteredMatchAll(AutocompleteScope scope) {
        String scopeValue = scope.name().toLowerCase();
        String marketValue = currentMarketKey();

        return Query.of(
                q -> q.bool(b -> b.filter(f -> f.term(t -> t.field(FIELD_SCOPE).value(FieldValue.of(scopeValue))))
                        .filter(f -> f.term(t -> t.field(FIELD_MARKET).value(FieldValue.of(marketValue))))
                        .must(m -> m.matchAll(ma -> ma))));
    }

    /**
     * 현재 마켓 키를 반환합니다.
     *
     * @return 마켓 키
     */
    @SneakyThrows
    private String currentMarketKey() {
        Market market = MarketContext.required().market();
        return market.name().toLowerCase();
    }

    /**
     * 필드 값 인자 함수를 빌드합니다.
     *
     * @return 함수 점수 목록
     */
    private List<FunctionScore> buildFieldValueFactorFunction() {
        return List.of(FunctionScore.of(f -> f.fieldValueFactor(fv -> fv.field(FIELD_COUNT)
                .factor(1.2F)
                .modifier(FieldValueFactorModifier.Log1p)
                .missing(1.0))));
    }
}
