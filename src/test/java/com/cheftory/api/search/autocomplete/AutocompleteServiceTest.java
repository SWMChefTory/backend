package com.cheftory.api.search.autocomplete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.search.exception.SearchException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeAutocompleteService Tests")
public class AutocompleteServiceTest {

    private AutocompleteRepository autocompleteRepository;
    private AutocompleteService autocompleteService;

    @BeforeEach
    void setUp() {
        autocompleteRepository = mock(AutocompleteRepository.class);
        autocompleteService = new AutocompleteService(autocompleteRepository);
    }

    @Nested
    @DisplayName("자동완성 검색")
    class Autocomplete {

        @Nested
        @DisplayName("Given - 유효한 검색어가 주어졌을 때")
        class GivenValidKeyword {

            private String keyword;
            private List<com.cheftory.api.search.autocomplete.Autocomplete> autocompletes;

            @BeforeEach
            void setUp() throws SearchException {
                keyword = "김치";

                com.cheftory.api.search.autocomplete.Autocomplete autocomplete1 =
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("1")
                                .text("김치찌개")
                                .count(100)
                                .build();

                com.cheftory.api.search.autocomplete.Autocomplete autocomplete2 =
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("2")
                                .text("김치전")
                                .count(80)
                                .build();

                com.cheftory.api.search.autocomplete.Autocomplete autocomplete3 =
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("3")
                                .text("김치볶음밥")
                                .count(60)
                                .build();

                autocompletes = List.of(autocomplete1, autocomplete2, autocomplete3);

                doReturn(autocompletes)
                        .when(autocompleteRepository)
                        .searchAutocomplete(any(AutocompleteScope.class), any(String.class), anyInt());
            }

            @Nested
            @DisplayName("When - 자동완성을 요청한다면")
            class WhenRequestingAutocomplete {

                @Test
                @DisplayName("Then - 자동완성 목록을 반환해야 한다")
                void thenShouldReturnAutocompleteList() throws SearchException {
                    List<com.cheftory.api.search.autocomplete.Autocomplete> result =
                            autocompleteService.autocomplete(AutocompleteScope.RECIPE, keyword);

                    assertThat(result).hasSize(3);
                    assertThat(result.get(0).getText()).isEqualTo("김치찌개");
                    assertThat(result.get(1).getText()).isEqualTo("김치전");
                    assertThat(result.get(2).getText()).isEqualTo("김치볶음밥");
                    verify(autocompleteRepository)
                            .searchAutocomplete(eq(AutocompleteScope.RECIPE), eq(keyword), eq(10));
                }
            }
        }

        @Nested
        @DisplayName("Given - 자동완성 결과가 없는 검색어가 주어졌을 때")
        class GivenKeywordWithNoResults {

            private String keyword;

            @BeforeEach
            void setUp() throws SearchException {
                keyword = "존재하지않는검색어";

                doReturn(List.of())
                        .when(autocompleteRepository)
                        .searchAutocomplete(any(AutocompleteScope.class), any(String.class), anyInt());
            }

            @Nested
            @DisplayName("When - 자동완성을 요청한다면")
            class WhenRequestingAutocomplete {

                @Test
                @DisplayName("Then - 빈 목록을 반환해야 한다")
                void thenShouldReturnEmptyList() throws SearchException {
                    List<com.cheftory.api.search.autocomplete.Autocomplete> result =
                            autocompleteService.autocomplete(AutocompleteScope.RECIPE, keyword);

                    assertThat(result).isEmpty();
                    verify(autocompleteRepository)
                            .searchAutocomplete(eq(AutocompleteScope.RECIPE), eq(keyword), eq(10));
                }
            }
        }

        @Nested
        @DisplayName("Given - 일부 일치하는 검색어가 주어졌을 때")
        class GivenPartialMatchKeyword {

            private String keyword;
            private List<com.cheftory.api.search.autocomplete.Autocomplete> autocompletes;

            @BeforeEach
            void setUp() throws SearchException {
                keyword = "파";

                com.cheftory.api.search.autocomplete.Autocomplete autocomplete1 =
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("1")
                                .text("파스타")
                                .count(120)
                                .build();

                com.cheftory.api.search.autocomplete.Autocomplete autocomplete2 =
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("2")
                                .text("파김치")
                                .count(50)
                                .build();

                autocompletes = List.of(autocomplete1, autocomplete2);

                doReturn(autocompletes)
                        .when(autocompleteRepository)
                        .searchAutocomplete(any(AutocompleteScope.class), any(String.class), anyInt());
            }

            @Nested
            @DisplayName("When - 자동완성을 요청한다면")
            class WhenRequestingAutocomplete {

                @Test
                @DisplayName("Then - 일치하는 자동완성 목록을 반환해야 한다")
                void thenShouldReturnMatchingAutocompleteList() throws SearchException {
                    List<com.cheftory.api.search.autocomplete.Autocomplete> result =
                            autocompleteService.autocomplete(AutocompleteScope.RECIPE, keyword);

                    assertThat(result).hasSize(2);
                    assertThat(result.get(0).getText()).isEqualTo("파스타");
                    assertThat(result.get(1).getText()).isEqualTo("파김치");
                    verify(autocompleteRepository)
                            .searchAutocomplete(eq(AutocompleteScope.RECIPE), eq(keyword), eq(10));
                }
            }
        }

        @Nested
        @DisplayName("Given - 많은 자동완성 결과가 있을 때")
        class GivenManyAutocompleteResults {

            private String keyword;
            private List<com.cheftory.api.search.autocomplete.Autocomplete> autocompletes;

            @BeforeEach
            void setUp() throws SearchException {
                keyword = "찌개";

                // 기본 제한만큼의 결과만 반환
                autocompletes = List.of(
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("1")
                                .text("김치찌개")
                                .count(100)
                                .build(),
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("2")
                                .text("된장찌개")
                                .count(90)
                                .build(),
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("3")
                                .text("부대찌개")
                                .count(85)
                                .build(),
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("4")
                                .text("순두부찌개")
                                .count(80)
                                .build(),
                        com.cheftory.api.search.autocomplete.Autocomplete.builder()
                                .id("5")
                                .text("청국장찌개")
                                .count(70)
                                .build());

                doReturn(autocompletes)
                        .when(autocompleteRepository)
                        .searchAutocomplete(any(AutocompleteScope.class), any(String.class), anyInt());
            }

            @Nested
            @DisplayName("When - 자동완성을 요청한다면")
            class WhenRequestingAutocomplete {

                @Test
                @DisplayName("Then - 제한된 결과를 반환해야 한다")
                void thenShouldReturnLimitedResults() throws SearchException {
                    List<com.cheftory.api.search.autocomplete.Autocomplete> result =
                            autocompleteService.autocomplete(AutocompleteScope.RECIPE, keyword);

                    assertThat(result).hasSize(5);
                    assertThat(result.getFirst().getText()).isEqualTo("김치찌개");
                    verify(autocompleteRepository)
                            .searchAutocomplete(eq(AutocompleteScope.RECIPE), eq(keyword), eq(10));
                }
            }
        }
    }
}
