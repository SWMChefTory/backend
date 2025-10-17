package com.cheftory.api.recipeinfo.search.autocomplete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipeinfo.search.exception.RecipeSearchErrorCode;
import com.cheftory.api.recipeinfo.search.exception.RecipeSearchException;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeAutocompleteRepository Tests")
public class RecipeAutocompleteRepositoryTest {

  @Mock private OpenSearchClient openSearchClient;
  @InjectMocks private RecipeAutocompleteRepository recipeAutocompleteRepository;

  @Nested
  @DisplayName("자동완성 검색")
  class SearchAutocomplete {

    @Nested
    @DisplayName("Given - 유효한 검색어가 주어졌을 때")
    class GivenValidKeyword {

      private String keyword;
      private Pageable pageable;
      private SearchResponse<RecipeAutocomplete> mockResponse;

      @BeforeEach
      void setUp() throws IOException {
        keyword = "김치";
        pageable = PageRequest.of(0, 5);

        RecipeAutocomplete auto1 =
            RecipeAutocomplete.builder().id("1").text("김치찌개").count(100).build();
        RecipeAutocomplete auto2 =
            RecipeAutocomplete.builder().id("2").text("김치전").count(80).build();
        RecipeAutocomplete auto3 =
            RecipeAutocomplete.builder().id("3").text("김치볶음밥").count(60).build();

        List<Hit<RecipeAutocomplete>> hits =
            List.of(createHit(auto1), createHit(auto2), createHit(auto3));

        HitsMetadata<RecipeAutocomplete> hitsMetadata = HitsMetadata.of(h -> h.hits(hits));
        mockResponse =
            SearchResponse.searchResponseOf(
                r ->
                    r.hits(hitsMetadata)
                        .took(1L)
                        .timedOut(false)
                        .shards(s -> s.total(1).successful(1).failed(0)));

        doReturn(mockResponse)
            .when(openSearchClient)
            .search(any(SearchRequest.class), eq(RecipeAutocomplete.class));
      }

      @Test
      @DisplayName("When - 자동완성을 검색하면 Then - 자동완성 목록이 반환된다")
      void whenSearchingAutocomplete_thenReturnsAutocompleteList() throws IOException {
        List<RecipeAutocomplete> result =
            recipeAutocompleteRepository.searchAutocomplete(keyword, pageable);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getText()).isEqualTo("김치찌개");
        assertThat(result.get(0).getCount()).isEqualTo(100);
        assertThat(result.get(1).getText()).isEqualTo("김치전");
        assertThat(result.get(2).getText()).isEqualTo("김치볶음밥");

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(requestCaptor.capture(), eq(RecipeAutocomplete.class));

        SearchRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.index()).contains("autocomplete");
        assertThat(capturedRequest.from()).isEqualTo(0);
        assertThat(capturedRequest.size()).isEqualTo(5);
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
        pageable = PageRequest.of(0, 5);

        HitsMetadata<RecipeAutocomplete> hitsMetadata = HitsMetadata.of(h -> h.hits(List.of()));
        SearchResponse<RecipeAutocomplete> mockResponse =
            SearchResponse.searchResponseOf(
                r ->
                    r.hits(hitsMetadata)
                        .took(1L)
                        .timedOut(false)
                        .shards(s -> s.total(1).successful(1).failed(0)));

        doReturn(mockResponse)
            .when(openSearchClient)
            .search(any(SearchRequest.class), eq(RecipeAutocomplete.class));
      }

      @Test
      @DisplayName("When - 자동완성을 검색하면 Then - 빈 목록이 반환된다")
      void whenSearchingAutocomplete_thenReturnsEmptyList() {
        List<RecipeAutocomplete> result =
            recipeAutocompleteRepository.searchAutocomplete(keyword, pageable);

        assertThat(result).isEmpty();
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
        pageable = PageRequest.of(0, 5);

        doThrow(new IOException("OpenSearch connection failed"))
            .when(openSearchClient)
            .search(any(SearchRequest.class), eq(RecipeAutocomplete.class));
      }

      @Test
      @DisplayName("When - 자동완성을 검색하면 Then - RuntimeException이 발생한다")
      void whenSearchingAutocomplete_thenThrowsRuntimeException() {
        assertThatThrownBy(() -> recipeAutocompleteRepository.searchAutocomplete(keyword, pageable))
            .isInstanceOf(RecipeSearchException.class)
            .hasFieldOrPropertyWithValue(
                "errorMessage", RecipeSearchErrorCode.RECIPE_AUTOCOMPLETE_FAILED);
      }
    }
  }

  private <T> Hit<T> createHit(T source) {
    return Hit.of(h -> h.source(source).index("autocomplete").id("test-id").score(1.0));
  }
}
