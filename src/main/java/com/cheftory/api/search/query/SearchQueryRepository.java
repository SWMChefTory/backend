package com.cheftory.api.search.query;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import org.opensearch.client.opensearch.core.get.GetResult;
import org.opensearch.client.opensearch.core.mget.MultiGetResponseItem;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.Pit;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * 검색 쿼리 OpenSearch 리포지토리.
 *
 * <p>OpenSearch를 사용하여 검색 쿼리를 관리합니다.</p>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchQueryRepository {

    /** 인덱스 이름. */
    private static final String INDEX = "search_query";

    /** PIT 유지 시간. */
    private static final Time PIT_KEEP_ALIVE = new Time.Builder().time("2m").build();

    /** 필드: 범위. */
    private static final String FIELD_SCOPE = "scope";

    /** 필드: 마켓. */
    private static final String FIELD_MARKET = "market";

    /** 필드: 키워드. */
    private static final String FIELD_KEYWORDS = "keywords";

    /** 필드: 채널 제목. */
    private static final String FIELD_CHANNEL_TITLE = "channel_title";

    /** 필드: 생성일시. */
    private static final String FIELD_CREATED_AT = "created_at";

    /** OpenSearch 클라이언트. */
    private final OpenSearchClient openSearchClient;

    /**
     * PIT(Point In Time) ID를 생성합니다.
     *
     * @return PIT ID
     * @throws SearchException 검색 예외
     */
    public String createPitId() throws SearchException {
        try {
            return openSearchClient
                    .createPit(CreatePitRequest.of(p -> p.index(INDEX).keepAlive(PIT_KEEP_ALIVE)))
                    .pitId();
        } catch (Exception e) {
            log.error("create PIT failed", e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED);
        }
    }

    /**
     * PIT를 닫습니다.
     *
     * @param pitId PIT ID
     */
    public void closePit(String pitId) {
        try {
            openSearchClient.deletePit(DeletePitRequest.of(c -> c.pitId(pitId)));
        } catch (Exception e) {
            log.warn("close PIT failed: pitId={}", pitId, e);
        }
    }

    /**
     * 키워드로 첫 번째 커서 페이지를 검색합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @param anchorNowIso 앵커 시간 (ISO 형식)
     * @param pitId PIT ID
     * @param pageable 페이지 정보
     * @return 검색 결과 히트 목록
     * @throws SearchException 검색 예외
     */
    public List<Hit<SearchQuery>> searchByKeywordCursorFirst(
            SearchQueryScope scope, String keyword, String anchorNowIso, String pitId, Pageable pageable)
            throws SearchException {
        Query q = buildFunctionScoreQuery(scope, keyword, anchorNowIso);
        return searchHits(cursorTemplateRequest(pitId, pageable, q, null, null), keyword);
    }

    /**
     * 여러 검색 쿼리를 조회합니다.
     *
     * @param ids 문서 ID 목록
     * @return 검색 쿼리 목록
     * @throws SearchException 검색 예외
     */
    public List<SearchQuery> mgetSearchQueries(List<String> ids) throws SearchException {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            var response = openSearchClient.mget(
                    mgetRequest -> mgetRequest
                            .index(INDEX)
                            .docs(ids.stream()
                                    .map(id -> org.opensearch.client.opensearch.core.mget.MultiGetOperation.of(
                                            op -> op.id(id)))
                                    .toList()),
                    SearchQuery.class);

            var docs = response.docs().stream()
                    .map(MultiGetResponseItem::result)
                    .filter(r -> r != null && r.found())
                    .map(GetResult::source)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            if (docs.size() != ids.size()) {
                var foundIds = docs.stream().map(SearchQuery::getId).collect(java.util.stream.Collectors.toSet());
                var missing = ids.stream().filter(id -> !foundIds.contains(id)).toList();
                log.warn("mget seeds missing: requested={}, found={}, missing={}", ids.size(), docs.size(), missing);
            }

            return docs;

        } catch (Exception e) {
            log.error("mget seeds failed: size={}", ids.size(), e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED);
        }
    }

    /**
     * 키워드로 키셋 기반 커서 페이지를 검색합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @param anchorNowIso 앵커 시간 (ISO 형식)
     * @param pitId PIT ID
     * @param lastScore 마지막 점수
     * @param lastId 마지막 ID
     * @param pageable 페이지 정보
     * @return 검색 결과 히트 목록
     * @throws SearchException 검색 예외
     */
    public List<Hit<SearchQuery>> searchByKeywordCursorKeyset(
            SearchQueryScope scope,
            String keyword,
            String anchorNowIso,
            String pitId,
            double lastScore,
            String lastId,
            Pageable pageable)
            throws SearchException {
        Query q = buildFunctionScoreQuery(scope, keyword, anchorNowIso);
        return searchHits(cursorTemplateRequest(pitId, pageable, q, lastScore, lastId), keyword);
    }

    /**
     * 후보군 첫 번째 커서 페이지를 검색합니다.
     *
     * @param scope 검색 범위
     * @param label 라벨
     * @param anchorNowIso 앵커 시간 (ISO 형식)
     * @param pitId PIT ID
     * @param profile 개인화 프로필
     * @param pageable 페이지 정보
     * @return 검색 결과 히트 목록
     * @throws SearchException 검색 예외
     */
    public List<Hit<SearchQuery>> searchCandidatesCursorFirst(
            SearchQueryScope scope,
            String label,
            String anchorNowIso,
            String pitId,
            PersonalizationProfile profile,
            Pageable pageable)
            throws SearchException {
        Query q = buildCandidatesFunctionScoreQuery(scope, label, anchorNowIso, profile);
        return searchHits(cursorTemplateRequest(pitId, pageable, q, null, null), "candidates");
    }

    /**
     * 후보군 키셋 기반 커서 페이지를 검색합니다.
     *
     * @param scope 검색 범위
     * @param label 라벨
     * @param anchorNowIso 앵커 시간 (ISO 형식)
     * @param pitId PIT ID
     * @param profile 개인화 프로필
     * @param lastScore 마지막 점수
     * @param lastId 마지막 ID
     * @param pageable 페이지 정보
     * @return 검색 결과 히트 목록
     * @throws SearchException 검색 예외
     */
    public List<Hit<SearchQuery>> searchCandidatesCursorKeyset(
            SearchQueryScope scope,
            String label,
            String anchorNowIso,
            String pitId,
            PersonalizationProfile profile,
            double lastScore,
            String lastId,
            Pageable pageable)
            throws SearchException {
        Query q = buildCandidatesFunctionScoreQuery(scope, label, anchorNowIso, profile);
        return searchHits(cursorTemplateRequest(pitId, pageable, q, lastScore, lastId), "candidates");
    }

    /**
     * 커서 템플릿 검색 요청을 생성합니다.
     *
     * @param pitId PIT ID
     * @param pageable 페이지 정보
     * @param query 쿼리
     * @param afterScore 이후 점수
     * @param afterId 이후 ID
     * @return 검색 요청
     */
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

    /**
     * 함수 점수 쿼리를 생성합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @param targetTime 목표 시간
     * @return 함수 점수 쿼리
     */
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

    /**
     * 후보군 함수 점수 쿼리를 생성합니다.
     *
     * @param scope 검색 범위
     * @param label 라벨
     * @param targetTime 목표 시간
     * @param profile 개인화 프로필
     * @return 함수 점수 쿼리
     */
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

    /**
     * Search After를 적용합니다.
     *
     * @param s 검색 요청 빌더
     * @param afterScore 이후 점수
     * @param afterId 이후 ID
     */
    private void applySearchAfter(SearchRequest.Builder s, Double afterScore, String afterId) {
        if (afterScore == null || afterId == null || afterId.isBlank()) return;
        s.searchAfter(
                List.of(FieldValue.of(v -> v.doubleValue(afterScore)), FieldValue.of(v -> v.stringValue(afterId))));
    }

    /**
     * 검색을 수행하고 히트 목록을 반환합니다.
     *
     * @param request 검색 요청
     * @param keywordForLog 로그용 검색어
     * @return 검색 결과 히트 목록
     * @throws SearchException 검색 예외
     */
    private List<Hit<SearchQuery>> searchHits(SearchRequest request, String keywordForLog) throws SearchException {
        try {
            return openSearchClient.search(request, SearchQuery.class).hits().hits();
        } catch (Exception e) {
            log.error(SearchErrorCode.SEARCH_FAILED.getMessage(), keywordForLog, e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED);
        }
    }

    /**
     * 현재 마켓 키를 반환합니다.
     *
     * @return 마켓 키
     * @throws Exception 마켓 컨텍스트 조회 실패 시 예외
     */
    @SneakyThrows
    private String currentMarketKey() {
        Market market = MarketContext.required().market();
        return market.name().toLowerCase();
    }

    /**
     * 가우스 감소 함수를 생성합니다.
     *
     * @param targetTime 목표 시간
     * @param scale 스케일
     * @param decay 감소율
     * @return 함수 점수 목록
     */
    private List<FunctionScore> buildGaussDecayFunction(String targetTime, String scale, Double decay) {
        return List.of(FunctionScore.of(functionScore ->
                functionScore.gauss(gauss -> gauss.field(FIELD_CREATED_AT).placement(placement -> placement
                        .origin(JsonData.of(targetTime))
                        .scale(JsonData.of(scale))
                        .decay(decay)))));
    }
}
