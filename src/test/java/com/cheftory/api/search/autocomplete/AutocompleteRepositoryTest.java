package com.cheftory.api.search.autocomplete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.MarketContextTestExtension;
import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
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

@ExtendWith({MockitoExtension.class, MarketContextTestExtension.class})
@DisplayName("RecipeAutocompleteRepository Tests")
public class AutocompleteRepositoryTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @InjectMocks
    private AutocompleteRepository autocompleteRepository;

    @Nested
    @DisplayName("자동완성 검색")
    class SearchAutocomplete {

        @Nested
        @DisplayName("Given - 유효한 검색어가 주어졌을 때")
        class GivenValidKeyword {

            private String keyword;
            private int limit;
            private SearchResponse<Autocomplete> mockResponse;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "김치";
                limit = 5;

                Autocomplete auto1 =
                        Autocomplete.builder().id("1").text("김치찌개").count(100).build();
                Autocomplete auto2 =
                        Autocomplete.builder().id("2").text("김치전").count(80).build();
                Autocomplete auto3 =
                        Autocomplete.builder().id("3").text("김치볶음밥").count(60).build();

                List<Hit<Autocomplete>> hits = List.of(createHit(auto1), createHit(auto2), createHit(auto3));

                HitsMetadata<Autocomplete> hitsMetadata = HitsMetadata.of(h -> h.hits(hits));
                mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Test
            @DisplayName("When - 자동완성을 검색하면 Then - 자동완성 목록이 반환된다")
            void whenSearchingAutocomplete_thenReturnsAutocompleteList() throws IOException {
                List<Autocomplete> result =
                        autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit);

                assertThat(result).hasSize(3);
                assertThat(result.get(0).getText()).isEqualTo("김치찌개");
                assertThat(result.get(0).getCount()).isEqualTo(100);
                assertThat(result.get(1).getText()).isEqualTo("김치전");
                assertThat(result.get(2).getText()).isEqualTo("김치볶음밥");

                ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
                verify(openSearchClient).search(requestCaptor.capture(), eq(Autocomplete.class));

                SearchRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.index()).contains("autocomplete");
                assertThat(capturedRequest.from()).isEqualTo(0);
                assertThat(capturedRequest.size()).isEqualTo(limit);
            }
        }

        @Nested
        @DisplayName("Given - 검색 결과가 없는 경우")
        class GivenNoResults {

            private String keyword;
            private int limit;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "존재하지않는검색어";
                limit = 5;

                HitsMetadata<Autocomplete> hitsMetadata = HitsMetadata.of(h -> h.hits(List.of()));
                SearchResponse<Autocomplete> mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Test
            @DisplayName("When - 자동완성을 검색하면 Then - 빈 목록이 반환된다")
            void whenSearchingAutocomplete_thenReturnsEmptyList() {
                List<Autocomplete> result =
                        autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit);

                assertThat(result).isEmpty();
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 null인 경우")
        class GivenNullKeyword {

            private String keyword;
            private int limit;
            private SearchResponse<Autocomplete> mockResponse;

            @BeforeEach
            void setUp() throws IOException {
                keyword = null;
                limit = 5;

                Autocomplete auto1 =
                        Autocomplete.builder().id("1").text("인기검색어1").count(200).build();
                Autocomplete auto2 =
                        Autocomplete.builder().id("2").text("인기검색어2").count(150).build();
                Autocomplete auto3 =
                        Autocomplete.builder().id("3").text("인기검색어3").count(100).build();

                List<Hit<Autocomplete>> hits = List.of(createHit(auto1), createHit(auto2), createHit(auto3));

                HitsMetadata<Autocomplete> hitsMetadata = HitsMetadata.of(h -> h.hits(hits));
                mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Test
            @DisplayName("When - 자동완성을 검색하면 Then - count 순으로 정렬된 목록이 반환된다")
            void whenSearchingAutocomplete_thenReturnsCountSortedList() throws IOException {
                List<Autocomplete> result =
                        autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit);

                assertThat(result).hasSize(3);
                assertThat(result.get(0).getText()).isEqualTo("인기검색어1");
                assertThat(result.get(0).getCount()).isEqualTo(200);
                assertThat(result.get(1).getText()).isEqualTo("인기검색어2");
                assertThat(result.get(2).getText()).isEqualTo("인기검색어3");

                ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
                verify(openSearchClient).search(requestCaptor.capture(), eq(Autocomplete.class));

                SearchRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.index()).contains("autocomplete");
                assertThat(capturedRequest.from()).isEqualTo(0);
                assertThat(capturedRequest.size()).isEqualTo(limit);
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 빈 문자열인 경우")
        class GivenBlankKeyword {

            private String keyword;
            private int limit;
            private SearchResponse<Autocomplete> mockResponse;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "   ";
                limit = 5;

                Autocomplete auto1 = Autocomplete.builder()
                        .id("1")
                        .text("빈문자열검색어1")
                        .count(300)
                        .build();
                Autocomplete auto2 = Autocomplete.builder()
                        .id("2")
                        .text("빈문자열검색어2")
                        .count(250)
                        .build();

                List<Hit<Autocomplete>> hits = List.of(createHit(auto1), createHit(auto2));

                HitsMetadata<Autocomplete> hitsMetadata = HitsMetadata.of(h -> h.hits(hits));
                mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(hitsMetadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Test
            @DisplayName("When - 자동완성을 검색하면 Then - count 순으로 정렬된 목록이 반환된다")
            void whenSearchingAutocomplete_thenReturnsCountSortedList() throws IOException {
                List<Autocomplete> result =
                        autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit);

                assertThat(result).hasSize(2);
                assertThat(result.get(0).getText()).isEqualTo("빈문자열검색어1");
                assertThat(result.get(0).getCount()).isEqualTo(300);
                assertThat(result.get(1).getText()).isEqualTo("빈문자열검색어2");
            }
        }

        @Nested
        @DisplayName("Given - OpenSearch에서 IOException이 발생한 경우")
        class GivenIOException {

            private String keyword;
            private int limit;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "test";
                limit = 5;

                doThrow(new IOException("OpenSearch connection failed"))
                        .when(openSearchClient)
                        .search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Test
            @DisplayName("When - 자동완성을 검색하면 Then - RuntimeException이 발생한다")
            void whenSearchingAutocomplete_thenThrowsRuntimeException() {
                assertThatThrownBy(() ->
                                autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit))
                        .isInstanceOf(SearchException.class)
                        .hasFieldOrPropertyWithValue("errorMessage", SearchErrorCode.AUTOCOMPLETE_FAILED);
            }
        }

        @Nested
        @DisplayName("Given - OpenSearch에서 RuntimeException이 발생한 경우")
        class GivenRuntimeException {

            private String keyword;
            private int limit;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "test";
                limit = 5;

                doThrow(new RuntimeException("OpenSearch internal error"))
                        .when(openSearchClient)
                        .search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Test
            @DisplayName("When - 자동완성을 검색하면 Then - RecipeSearchException이 발생한다")
            void whenSearchingAutocomplete_thenThrowsRecipeSearchException() {
                assertThatThrownBy(() ->
                                autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit))
                        .isInstanceOf(SearchException.class)
                        .hasFieldOrPropertyWithValue("errorMessage", SearchErrorCode.AUTOCOMPLETE_FAILED);
            }
        }
    }

    private <T> Hit<T> createHit(T source) {
        return Hit.of(h -> h.source(source).index("autocomplete").id("test-id").score(1.0));
    }
}
