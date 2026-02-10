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
@DisplayName("AutocompleteRepository 테스트")
public class AutocompleteRepositoryTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @InjectMocks
    private AutocompleteRepository autocompleteRepository;

    @Nested
    @DisplayName("자동완성 검색 (searchAutocomplete)")
    class SearchAutocomplete {

        @Nested
        @DisplayName("Given - 유효한 검색어가 주어졌을 때")
        class GivenValidKeyword {
            String keyword;
            int limit;
            SearchResponse<Autocomplete> mockResponse;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "김치";
                limit = 5;
                Autocomplete a1 =
                        Autocomplete.builder().id("1").text("김치찌개").count(100).build();
                Autocomplete a2 =
                        Autocomplete.builder().id("2").text("김치전").count(80).build();
                Autocomplete a3 =
                        Autocomplete.builder().id("3").text("김치볶음밥").count(60).build();

                List<Hit<Autocomplete>> hits = List.of(createHit(a1), createHit(a2), createHit(a3));
                HitsMetadata<Autocomplete> metadata = HitsMetadata.of(h -> h.hits(hits));
                mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(metadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {
                List<Autocomplete> result;

                @BeforeEach
                void setUp() throws SearchException {
                    result = autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit);
                }

                @Test
                @DisplayName("Then - 자동완성 목록을 반환한다")
                void thenReturnsList() throws IOException {
                    assertThat(result).hasSize(3);
                    assertThat(result.get(0).getText()).isEqualTo("김치찌개");
                    assertThat(result.get(1).getText()).isEqualTo("김치전");
                    assertThat(result.get(2).getText()).isEqualTo("김치볶음밥");

                    ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
                    verify(openSearchClient).search(captor.capture(), eq(Autocomplete.class));

                    SearchRequest req = captor.getValue();
                    assertThat(req.index()).contains("autocomplete");
                    assertThat(req.from()).isEqualTo(0);
                    assertThat(req.size()).isEqualTo(limit);
                }
            }
        }

        @Nested
        @DisplayName("Given - 결과가 없을 때")
        class GivenNoResults {
            String keyword;
            int limit;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "없는검색어";
                limit = 5;
                HitsMetadata<Autocomplete> metadata = HitsMetadata.of(h -> h.hits(List.of()));
                SearchResponse<Autocomplete> mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(metadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmpty() throws SearchException {
                    List<Autocomplete> result =
                            autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit);
                    assertThat(result).isEmpty();
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 null일 때")
        class GivenNullKeyword {
            String keyword;
            int limit;

            @BeforeEach
            void setUp() throws IOException {
                keyword = null;
                limit = 5;
                Autocomplete a1 =
                        Autocomplete.builder().id("1").text("인기1").count(200).build();
                Autocomplete a2 =
                        Autocomplete.builder().id("2").text("인기2").count(150).build();

                List<Hit<Autocomplete>> hits = List.of(createHit(a1), createHit(a2));
                HitsMetadata<Autocomplete> metadata = HitsMetadata.of(h -> h.hits(hits));
                SearchResponse<Autocomplete> mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(metadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {

                @Test
                @DisplayName("Then - 인기 검색어(count 순)를 반환한다")
                void thenReturnsPopular() throws SearchException {
                    List<Autocomplete> result =
                            autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit);
                    assertThat(result).hasSize(2);
                    assertThat(result.get(0).getText()).isEqualTo("인기1");
                    assertThat(result.get(1).getText()).isEqualTo("인기2");
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 빈 문자열일 때")
        class GivenBlankKeyword {
            String keyword;
            int limit;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "   ";
                limit = 5;
                Autocomplete a1 =
                        Autocomplete.builder().id("1").text("인기1").count(300).build();

                List<Hit<Autocomplete>> hits = List.of(createHit(a1));
                HitsMetadata<Autocomplete> metadata = HitsMetadata.of(h -> h.hits(hits));
                SearchResponse<Autocomplete> mockResponse = SearchResponse.searchResponseOf(
                        r -> r.hits(metadata).took(1L).timedOut(false).shards(s -> s.total(1)
                                .successful(1)
                                .failed(0)));

                doReturn(mockResponse).when(openSearchClient).search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {

                @Test
                @DisplayName("Then - 인기 검색어(count 순)를 반환한다")
                void thenReturnsPopular() throws SearchException {
                    List<Autocomplete> result =
                            autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit);
                    assertThat(result).hasSize(1);
                    assertThat(result.getFirst().getText()).isEqualTo("인기1");
                }
            }
        }

        @Nested
        @DisplayName("Given - OpenSearch IOException 발생 시")
        class GivenIOException {
            String keyword;
            int limit;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "test";
                limit = 5;
                doThrow(new IOException("fail"))
                        .when(openSearchClient)
                        .search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {

                @Test
                @DisplayName("Then - AUTOCOMPLETE_FAILED 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() ->
                                    autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit))
                            .isInstanceOf(SearchException.class)
                            .hasFieldOrPropertyWithValue("error", SearchErrorCode.AUTOCOMPLETE_FAILED);
                }
            }
        }

        @Nested
        @DisplayName("Given - OpenSearch RuntimeException 발생 시")
        class GivenRuntimeException {
            String keyword;
            int limit;

            @BeforeEach
            void setUp() throws IOException {
                keyword = "test";
                limit = 5;
                doThrow(new RuntimeException("fail"))
                        .when(openSearchClient)
                        .search(any(SearchRequest.class), eq(Autocomplete.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {

                @Test
                @DisplayName("Then - AUTOCOMPLETE_FAILED 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() ->
                                    autocompleteRepository.searchAutocomplete(AutocompleteScope.RECIPE, keyword, limit))
                            .isInstanceOf(SearchException.class)
                            .hasFieldOrPropertyWithValue("error", SearchErrorCode.AUTOCOMPLETE_FAILED);
                }
            }
        }
    }

    private <T> Hit<T> createHit(T source) {
        return Hit.of(h -> h.source(source).index("autocomplete").id("id").score(1.0));
    }
}
