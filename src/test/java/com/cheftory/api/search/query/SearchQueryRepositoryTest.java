package com.cheftory.api.search.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.io.IOException;
import java.util.List;
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
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.TotalHits;
import org.opensearch.client.opensearch.core.search.TotalHitsRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeSearchRepository Tests")
public class SearchQueryRepositoryTest {

  @Mock private OpenSearchClient openSearchClient;
  @InjectMocks private SearchQueryRepository searchQueryRepository;

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

        SearchQuery recipe1 = SearchQuery.builder().id("1").searchText("김치찌개").build();
        SearchQuery recipe2 = SearchQuery.builder().id("2").searchText("김치찌개 레시피").build();
        SearchQuery recipe3 = SearchQuery.builder().id("3").searchText("맛있는 김치찌개").build();

        List<Hit<SearchQuery>> hits =
            List.of(createHit(recipe1), createHit(recipe2), createHit(recipe3));

        TotalHits totalHits = TotalHits.of(t -> t.value(3).relation(TotalHitsRelation.Eq));
        HitsMetadata<SearchQuery> hitsMetadata =
            HitsMetadata.of(h -> h.hits(hits).total(totalHits));

        mockResponse =
            SearchResponse.searchResponseOf(
                r ->
                    r.hits(hitsMetadata)
                        .took(1L)
                        .timedOut(false)
                        .shards(s -> s.total(1).successful(1).failed(0)));

        doReturn(mockResponse)
            .when(openSearchClient)
            .search(any(SearchRequest.class), eq(SearchQuery.class));
      }

      @Test
      @DisplayName("When - 검색을 수행하면 Then - 검색 결과가 페이지로 반환된다")
      void whenSearching_thenReturnsPagedResults() throws IOException {
        Page<SearchQuery> result = searchQueryRepository.searchByKeyword(keyword, pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().getFirst().getSearchText()).isEqualTo("김치찌개");

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(requestCaptor.capture(), eq(SearchQuery.class));

        SearchRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.index()).contains("recipes");
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
        SearchResponse<SearchQuery> mockResponse =
            SearchResponse.searchResponseOf(
                r ->
                    r.hits(hitsMetadata)
                        .took(1L)
                        .timedOut(false)
                        .shards(s -> s.total(1).successful(1).failed(0)));

        doReturn(mockResponse)
            .when(openSearchClient)
            .search(any(SearchRequest.class), eq(SearchQuery.class));
      }

      @Test
      @DisplayName("When - 검색을 수행하면 Then - 빈 페이지가 반환된다")
      void whenSearching_thenReturnsEmptyPage() {
        Page<SearchQuery> result = searchQueryRepository.searchByKeyword(keyword, pageable);

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
        assertThatThrownBy(() -> searchQueryRepository.searchByKeyword(keyword, pageable))
            .isInstanceOf(SearchException.class)
            .hasFieldOrPropertyWithValue("errorMessage", SearchErrorCode.SEARCH_FAILED);
      }
    }
  }

  private <T> Hit<T> createHit(T source) {
    return Hit.of(h -> h.source(source).index("recipes").id("test-id").score(1.0));
  }
}
