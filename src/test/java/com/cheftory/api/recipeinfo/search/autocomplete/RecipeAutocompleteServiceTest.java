package com.cheftory.api.recipeinfo.search.autocomplete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipeinfo.search.RecipeSearchPageRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

@DisplayName("RecipeAutocompleteService Tests")
public class RecipeAutocompleteServiceTest {

  private RecipeAutocompleteRepository recipeAutocompleteRepository;
  private RecipeAutocompleteService recipeAutocompleteService;

  @BeforeEach
  void setUp() {
    recipeAutocompleteRepository = mock(RecipeAutocompleteRepository.class);
    recipeAutocompleteService = new RecipeAutocompleteService(recipeAutocompleteRepository);
  }

  @Nested
  @DisplayName("자동완성 검색")
  class Autocomplete {

    @Nested
    @DisplayName("Given - 유효한 검색어가 주어졌을 때")
    class GivenValidKeyword {

      private String keyword;
      private List<RecipeAutocomplete> autocompletes;

      @BeforeEach
      void setUp() {
        keyword = "김치";

        RecipeAutocomplete autocomplete1 =
            RecipeAutocomplete.builder().id("1").text("김치찌개").count(100).build();

        RecipeAutocomplete autocomplete2 =
            RecipeAutocomplete.builder().id("2").text("김치전").count(80).build();

        RecipeAutocomplete autocomplete3 =
            RecipeAutocomplete.builder().id("3").text("김치볶음밥").count(60).build();

        autocompletes = List.of(autocomplete1, autocomplete2, autocomplete3);

        doReturn(autocompletes)
            .when(recipeAutocompleteRepository)
            .searchAutocomplete(any(String.class), any(Pageable.class));
      }

      @Nested
      @DisplayName("When - 자동완성을 요청한다면")
      class WhenRequestingAutocomplete {

        @Test
        @DisplayName("Then - 자동완성 목록을 반환해야 한다")
        void thenShouldReturnAutocompleteList() {
          List<RecipeAutocomplete> result = recipeAutocompleteService.autocomplete(keyword);

          assertThat(result).hasSize(3);
          assertThat(result.get(0).getText()).isEqualTo("김치찌개");
          assertThat(result.get(1).getText()).isEqualTo("김치전");
          assertThat(result.get(2).getText()).isEqualTo("김치볶음밥");
          verify(recipeAutocompleteRepository)
              .searchAutocomplete(keyword, RecipeSearchPageRequest.create(0));
        }
      }
    }

    @Nested
    @DisplayName("Given - 자동완성 결과가 없는 검색어가 주어졌을 때")
    class GivenKeywordWithNoResults {

      private String keyword;

      @BeforeEach
      void setUp() {
        keyword = "존재하지않는검색어";

        doReturn(List.of())
            .when(recipeAutocompleteRepository)
            .searchAutocomplete(any(String.class), any(Pageable.class));
      }

      @Nested
      @DisplayName("When - 자동완성을 요청한다면")
      class WhenRequestingAutocomplete {

        @Test
        @DisplayName("Then - 빈 목록을 반환해야 한다")
        void thenShouldReturnEmptyList() {
          List<RecipeAutocomplete> result = recipeAutocompleteService.autocomplete(keyword);

          assertThat(result).isEmpty();
          verify(recipeAutocompleteRepository)
              .searchAutocomplete(keyword, RecipeSearchPageRequest.create(0));
        }
      }
    }

    @Nested
    @DisplayName("Given - 일부 일치하는 검색어가 주어졌을 때")
    class GivenPartialMatchKeyword {

      private String keyword;
      private List<RecipeAutocomplete> autocompletes;

      @BeforeEach
      void setUp() {
        keyword = "파";

        RecipeAutocomplete autocomplete1 =
            RecipeAutocomplete.builder().id("1").text("파스타").count(120).build();

        RecipeAutocomplete autocomplete2 =
            RecipeAutocomplete.builder().id("2").text("파김치").count(50).build();

        autocompletes = List.of(autocomplete1, autocomplete2);

        doReturn(autocompletes)
            .when(recipeAutocompleteRepository)
            .searchAutocomplete(any(String.class), any(Pageable.class));
      }

      @Nested
      @DisplayName("When - 자동완성을 요청한다면")
      class WhenRequestingAutocomplete {

        @Test
        @DisplayName("Then - 일치하는 자동완성 목록을 반환해야 한다")
        void thenShouldReturnMatchingAutocompleteList() {
          List<RecipeAutocomplete> result = recipeAutocompleteService.autocomplete(keyword);

          assertThat(result).hasSize(2);
          assertThat(result.get(0).getText()).isEqualTo("파스타");
          assertThat(result.get(1).getText()).isEqualTo("파김치");
          verify(recipeAutocompleteRepository)
              .searchAutocomplete(keyword, RecipeSearchPageRequest.create(0));
        }
      }
    }

    @Nested
    @DisplayName("Given - 많은 자동완성 결과가 있을 때")
    class GivenManyAutocompleteResults {

      private String keyword;
      private List<RecipeAutocomplete> autocompletes;

      @BeforeEach
      void setUp() {
        keyword = "찌개";

        // 페이지 크기만큼의 결과만 반환
        autocompletes =
            List.of(
                RecipeAutocomplete.builder().id("1").text("김치찌개").count(100).build(),
                RecipeAutocomplete.builder().id("2").text("된장찌개").count(90).build(),
                RecipeAutocomplete.builder().id("3").text("부대찌개").count(85).build(),
                RecipeAutocomplete.builder().id("4").text("순두부찌개").count(80).build(),
                RecipeAutocomplete.builder().id("5").text("청국장찌개").count(70).build());

        doReturn(autocompletes)
            .when(recipeAutocompleteRepository)
            .searchAutocomplete(any(String.class), any(Pageable.class));
      }

      @Nested
      @DisplayName("When - 자동완성을 요청한다면")
      class WhenRequestingAutocomplete {

        @Test
        @DisplayName("Then - 페이지 크기만큼의 결과를 반환해야 한다")
        void thenShouldReturnLimitedResults() {
          List<RecipeAutocomplete> result = recipeAutocompleteService.autocomplete(keyword);

          assertThat(result).hasSize(5);
          assertThat(result.get(0).getText()).isEqualTo("김치찌개");
          verify(recipeAutocompleteRepository)
              .searchAutocomplete(keyword, RecipeSearchPageRequest.create(0));
        }
      }
    }
  }
}
