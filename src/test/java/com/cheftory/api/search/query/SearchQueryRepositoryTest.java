package com.cheftory.api.search.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.MarketContextTestExtension;
import com.cheftory.api.ranking.personalization.PersonalizationProfile;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.CreatePitRequest;
import org.opensearch.client.opensearch.core.CreatePitResponse;
import org.opensearch.client.opensearch.core.DeletePitRequest;
import org.opensearch.client.opensearch.core.MgetRequest;
import org.opensearch.client.opensearch.core.MgetResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.get.GetResult;
import org.opensearch.client.opensearch.core.mget.MultiGetResponseItem;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.Pit;
import org.opensearch.client.opensearch.core.search.TotalHits;
import org.opensearch.client.opensearch.core.search.TotalHitsRelation;
import org.opensearch.client.util.ObjectBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith({MockitoExtension.class, MarketContextTestExtension.class})
@DisplayName("RecipeSearchRepository Tests")
public class SearchQueryRepositoryTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @InjectMocks
    private SearchQueryRepository searchQueryRepository;

    @Nested
    @DisplayName("레시피 검색")
    class SearchByKeyword {

        @Nested
        @DisplayName("Given - 유효한 검색어와 페이징 정보가 주어졌을 때")
        class GivenValidKeywordAndPageable {

            private String keyword;
            private Pageable pageable;
            private SearchResponse<SearchQuery> mockResponse;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "김치찌개";
                pageable = PageRequest.of(0, 3);

                SearchQuery recipe1 =
                        SearchQuery.builder().id("1").searchText("김치찌개").build();
                SearchQuery recipe2 =
                        SearchQuery.builder().id("2").searchText("김치찌개 레시피").build();
                SearchQuery recipe3 =
                        SearchQuery.builder().id("3").searchText("맛있는 김치찌개").build();

                List<Hit<SearchQuery>> hits = List.of(createHit(recipe1), createHit(recipe2), createHit(recipe3));

                TotalHits totalHits = TotalHits.of(t -> t.value(3).relation(TotalHitsRelation.Eq));
                HitsMetadata<SearchQuery> hitsMetadata =
                        HitsMetadata.of(h -> h.hits(hits).total(totalHits));

                mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(SearchQuery.class));
            }

            @Test
            @DisplayName("When - 검색을 수행하면 Then - 검색 결과가 페이지로 반환된다")
            void whenSearching_thenReturnsPagedResults() throws IOException {
                Page<SearchQuery> result =
                        searchQueryRepository.searchByKeyword(SearchQueryScope.RECIPE, keyword, pageable);

                assertThat(result.getContent()).hasSize(3);
                assertThat(result.getTotalElements()).isEqualTo(3);
                assertThat(result.getContent().getFirst().getSearchText()).isEqualTo("김치찌개");

                ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
                verify(openSearchClient).search(requestCaptor.capture(), eq(SearchQuery.class));

                SearchRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.index()).contains("search_query");
                assertThat(capturedRequest.from()).isEqualTo(0);
                assertThat(capturedRequest.size()).isEqualTo(3);
            }
        }

        @Nested
        @DisplayName("Given - 검색 결과가 없는 경우")
        class GivenNoResults {

            private String keyword;
            private Pageable pageable;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "존재하지않는검색어";
                pageable = PageRequest.of(0, 10);

                TotalHits totalHits = TotalHits.of(t -> t.value(0).relation(TotalHitsRelation.Eq));
                HitsMetadata<SearchQuery> hitsMetadata =
                        HitsMetadata.of(h -> h.hits(List.of()).total(totalHits));
                SearchResponse<SearchQuery> mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(SearchQuery.class));
            }

            @Test
            @DisplayName("When - 검색을 수행하면 Then - 빈 페이지가 반환된다")
            void whenSearching_thenReturnsEmptyPage() {
                Page<SearchQuery> result =
                        searchQueryRepository.searchByKeyword(SearchQueryScope.RECIPE, keyword, pageable);

                assertThat(result.getContent()).isEmpty();
                assertThat(result.getTotalElements()).isEqualTo(0);
            }
        }

        @Nested
        @DisplayName("Given - OpenSearch에서 IOException이 발생한 경우")
        class GivenIOException {

            private String keyword;
            private Pageable pageable;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "test";
                pageable = PageRequest.of(0, 10);

                doThrow(new IOException("OpenSearch connection failed"))
                        .when(openSearchClient)
                        .search(any(SearchRequest.class), eq(SearchQuery.class));
            }

            @Test
            @DisplayName("When - 검색을 수행하면 Then - RecipeSearchException 발생한다")
            void whenSearching_thenThrowsRuntimeException() {
                assertThatThrownBy(
                                () -> searchQueryRepository.searchByKeyword(SearchQueryScope.RECIPE, keyword, pageable))
                        .isInstanceOf(SearchException.class)
                        .hasFieldOrPropertyWithValue("errorMessage", SearchErrorCode.SEARCH_FAILED);
            }
        }

        @Test
        @DisplayName("When - 커서 첫 페이지를 조회하면 Then - PIT 기반 검색 요청이 생성된다")
        void whenSearchingFirstCursor_thenCreatesPitRequest() throws IOException {
            String keyword = "김치찌개";
            Pageable pageable = PageRequest.of(0, 2);

            SearchQuery recipe1 =
                    SearchQuery.builder().id("1").searchText("김치찌개").build();
            List<Hit<SearchQuery>> hits = List.of(createHit(recipe1));
            TotalHits totalHits = TotalHits.of(t -> t.value(1).relation(TotalHitsRelation.Eq));
            HitsMetadata<SearchQuery> hitsMetadata =
                    HitsMetadata.of(h -> h.hits(hits).total(totalHits));
            SearchResponse<SearchQuery> mockResponse = SearchResponse.searchResponseOf(
                    r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                            .successful(1)
                            .failed(0)));

            doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(SearchQuery.class));

            searchQueryRepository.searchByKeywordCursorFirst(
                    SearchQueryScope.RECIPE, keyword, "now", "pit-1", pageable);

            ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
            verify(openSearchClient).search(requestCaptor.capture(), eq(SearchQuery.class));

            SearchRequest capturedRequest = requestCaptor.getValue();
            Pit pit = capturedRequest.pit();
            assertThat(pit).isNotNull();
            assertThat(pit.id()).isEqualTo("pit-1");
        }

        @Test
        @DisplayName("When - 커서 keyset을 조회하면 Then - search_after가 포함된다")
        void whenSearchingCursorKeyset_thenUsesSearchAfter() throws IOException {
            String keyword = "김치찌개";
            Pageable pageable = PageRequest.of(0, 2);

            SearchQuery recipe1 =
                    SearchQuery.builder().id("1").searchText("김치찌개").build();
            List<Hit<SearchQuery>> hits = List.of(createHit(recipe1));
            TotalHits totalHits = TotalHits.of(t -> t.value(1).relation(TotalHitsRelation.Eq));
            HitsMetadata<SearchQuery> hitsMetadata =
                    HitsMetadata.of(h -> h.hits(hits).total(totalHits));
            SearchResponse<SearchQuery> mockResponse = SearchResponse.searchResponseOf(
                    r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                            .successful(1)
                            .failed(0)));

            doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(SearchQuery.class));

            searchQueryRepository.searchByKeywordCursorKeyset(
                    SearchQueryScope.RECIPE, keyword, "now", "pit-1", 1.2, "id-10", pageable);

            ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
            verify(openSearchClient).search(requestCaptor.capture(), eq(SearchQuery.class));

            SearchRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.searchAfter()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("PIT 관리")
    class PitManagement {

        @Test
        @DisplayName("createPitId - PIT id를 반환한다")
        void createPitIdReturnsPitId() throws IOException {
            CreatePitResponse response = org.mockito.Mockito.mock(CreatePitResponse.class);
            doReturn("pit-1").when(response).pitId();
            doReturn(response).when(openSearchClient).createPit(any(CreatePitRequest.class));

            String result = searchQueryRepository.createPitId();

            assertThat(result).isEqualTo("pit-1");
            ArgumentCaptor<CreatePitRequest> requestCaptor = ArgumentCaptor.forClass(CreatePitRequest.class);
            verify(openSearchClient).createPit(requestCaptor.capture());
            CreatePitRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.index()).contains("search_query");
            assertThat(capturedRequest.keepAlive()).isNotNull();
        }

        @Test
        @DisplayName("closePit - PIT 삭제 요청을 보낸다")
        void closePitSendsDeleteRequest() throws IOException {
            searchQueryRepository.closePit("pit-1");

            ArgumentCaptor<DeletePitRequest> requestCaptor = ArgumentCaptor.forClass(DeletePitRequest.class);
            verify(openSearchClient).deletePit(requestCaptor.capture());
            assertThat(requestCaptor.getValue().pitId()).contains("pit-1");
        }
    }

    @Nested
    @DisplayName("mgetSearchQueries")
    class MgetSearchQueries {

        @Test
        @DisplayName("ids로 문서를 조회한다")
        void mgetReturnsSources() throws IOException {
            SearchQuery query1 =
                    SearchQuery.builder().id("id-1").searchText("kimchi").build();
            SearchQuery query2 =
                    SearchQuery.builder().id("id-2").searchText("ramen").build();

            MultiGetResponseItem<SearchQuery> item1 = MultiGetResponseItem.of(b -> b.result(GetResult.getResultOf(
                    r -> r.index("search_query").id("id-1").found(true).source(query1))));
            MultiGetResponseItem<SearchQuery> item2 = MultiGetResponseItem.of(b -> b.result(GetResult.getResultOf(
                    r -> r.index("search_query").id("id-2").found(true).source(query2))));

            MgetResponse<SearchQuery> response = MgetResponse.of(b -> b.docs(List.of(item1, item2)));
            doReturn(response)
                    .when(openSearchClient)
                    .mget(
                            org.mockito.ArgumentMatchers
                                    .<Function<MgetRequest.Builder, ObjectBuilder<MgetRequest>>>any(),
                            eq(SearchQuery.class));

            List<SearchQuery> result = searchQueryRepository.mgetSearchQueries(List.of("id-1", "id-2"));

            assertThat(result).containsExactly(query1, query2);
        }
    }

    @Nested
    @DisplayName("추천 후보 검색")
    class SearchCandidates {

        @Test
        @DisplayName("첫 페이지는 PIT 기반 검색 요청을 만든다")
        void searchCandidatesCursorFirstCreatesPitRequest() throws IOException {
            Pageable pageable = PageRequest.of(0, 2);
            PersonalizationProfile profile = new PersonalizationProfile(List.of("kimchi"), List.of("channel"));

            SearchQuery recipe1 =
                    SearchQuery.builder().id("1").searchText("kimchi").build();
            List<Hit<SearchQuery>> hits = List.of(createHit(recipe1));
            TotalHits totalHits = TotalHits.of(t -> t.value(1).relation(TotalHitsRelation.Eq));
            HitsMetadata<SearchQuery> hitsMetadata =
                    HitsMetadata.of(h -> h.hits(hits).total(totalHits));
            SearchResponse<SearchQuery> mockResponse = SearchResponse.searchResponseOf(
                    r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                            .successful(1)
                            .failed(0)));

            doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(SearchQuery.class));

            searchQueryRepository.searchCandidatesCursorFirst(
                    SearchQueryScope.RECIPE, "label", "now", "pit-1", profile, pageable);

            ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
            verify(openSearchClient).search(requestCaptor.capture(), eq(SearchQuery.class));

            SearchRequest capturedRequest = requestCaptor.getValue();
            Pit pit = capturedRequest.pit();
            assertThat(pit).isNotNull();
            assertThat(pit.id()).isEqualTo("pit-1");
        }

        @Test
        @DisplayName("keyset 조회는 search_after가 포함된다")
        void searchCandidatesCursorKeysetUsesSearchAfter() throws IOException {
            Pageable pageable = PageRequest.of(0, 2);
            PersonalizationProfile profile = new PersonalizationProfile(List.of("kimchi"), List.of("channel"));

            SearchQuery recipe1 =
                    SearchQuery.builder().id("1").searchText("kimchi").build();
            List<Hit<SearchQuery>> hits = List.of(createHit(recipe1));
            TotalHits totalHits = TotalHits.of(t -> t.value(1).relation(TotalHitsRelation.Eq));
            HitsMetadata<SearchQuery> hitsMetadata =
                    HitsMetadata.of(h -> h.hits(hits).total(totalHits));
            SearchResponse<SearchQuery> mockResponse = SearchResponse.searchResponseOf(
                    r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                            .successful(1)
                            .failed(0)));

            doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(SearchQuery.class));

            searchQueryRepository.searchCandidatesCursorKeyset(
                    SearchQueryScope.RECIPE, "label", "now", "pit-1", profile, 1.1, "id-10", pageable);

            ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
            verify(openSearchClient).search(requestCaptor.capture(), eq(SearchQuery.class));

            SearchRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.searchAfter()).hasSize(2);
        }
    }

    private <T> Hit<T> createHit(T source) {
        return Hit.of(h -> h.source(source).index("search_query").id("test-id").score(1.0));
    }
}
