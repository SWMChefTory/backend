package com.cheftory.api.recipeinfo.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipeinfo.search.entity.RecipeSearch;
import com.cheftory.api.recipeinfo.search.history.RecipeSearchHistoryService;
import com.cheftory.api.recipeinfo.search.utils.RecipeSearchPageRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeSearchService Tests")
public class RecipeSearchServiceTest {

  @Mock private RecipeSearchRepository recipeSearchRepository;
  @Mock private RecipeSearchHistoryService recipeSearchHistoryService;

  @InjectMocks private RecipeSearchService recipeSearchService;

  @Nested
  @DisplayName("레시피 검색")
  class SearchRecipes {

    @Test
    @DisplayName("Given - 유효한 검색어가 주어졌을 때 When - 첫 페이지를 검색한다면 Then - 검색 결과를 반환해야 한다")
    void givenValidKeyword_whenSearchingFirstPage_thenShouldReturnSearchResults() {
      UUID userId = UUID.randomUUID();
      String keyword = "김치찌개";

      RecipeSearch recipe1 = RecipeSearch.builder().id("1").searchText("김치찌개").build();
      RecipeSearch recipe2 = RecipeSearch.builder().id("2").searchText("김치찌개 레시피").build();
      RecipeSearch recipe3 = RecipeSearch.builder().id("3").searchText("맛있는 김치찌개").build();

      List<RecipeSearch> content = List.of(recipe1, recipe2, recipe3);
      Pageable pageable = RecipeSearchPageRequest.create(0);
      Page<RecipeSearch> searchResults = new PageImpl<>(content, pageable, 3);

      doReturn(searchResults)
          .when(recipeSearchRepository)
          .searchByKeyword(eq(keyword), any(Pageable.class));

      Page<RecipeSearch> result = recipeSearchService.search(userId, keyword, 0);

      assertThat(result.getContent()).hasSize(3);
      assertThat(result.getContent().get(0).getSearchText()).isEqualTo("김치찌개");
      assertThat(result.getContent().get(1).getSearchText()).isEqualTo("김치찌개 레시피");
      assertThat(result.getContent().get(2).getSearchText()).isEqualTo("맛있는 김치찌개");
      assertThat(result.getTotalElements()).isEqualTo(3);
      verify(recipeSearchRepository).searchByKeyword(keyword, RecipeSearchPageRequest.create(0));
      verify(recipeSearchHistoryService).create(userId, keyword);
    }

    @Test
    @DisplayName("Given - 검색 결과가 없는 검색어가 주어졌을 때 When - 검색한다면 Then - 빈 페이지를 반환해야 한다")
    void givenKeywordWithNoResults_whenSearching_thenShouldReturnEmptyPage() {
      UUID userId = UUID.randomUUID();
      String keyword = "존재하지않는검색어";

      Pageable pageable = RecipeSearchPageRequest.create(0);
      Page<RecipeSearch> emptyResults = new PageImpl<>(List.of(), pageable, 0);

      doReturn(emptyResults)
          .when(recipeSearchRepository)
          .searchByKeyword(eq(keyword), any(Pageable.class));

      Page<RecipeSearch> result = recipeSearchService.search(userId, keyword, 0);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isEqualTo(0);
      verify(recipeSearchRepository).searchByKeyword(keyword, RecipeSearchPageRequest.create(0));
      verify(recipeSearchHistoryService).create(userId, keyword);
    }

    @Test
    @DisplayName("Given - 여러 페이지의 검색 결과가 있을 때 When - 첫 페이지를 검색한다면 Then - 첫 페이지 결과를 반환해야 한다")
    void givenMultiplePages_whenSearchingFirstPage_thenShouldReturnFirstPageResults() {
      UUID userId = UUID.randomUUID();
      String keyword = "찌개";

      List<RecipeSearch> firstPageContent =
          List.of(
              RecipeSearch.builder().id("1").searchText("김치찌개").build(),
              RecipeSearch.builder().id("2").searchText("된장찌개").build(),
              RecipeSearch.builder().id("3").searchText("부대찌개").build());

      Pageable firstPageable = RecipeSearchPageRequest.create(0);
      Page<RecipeSearch> firstPageResults = new PageImpl<>(firstPageContent, firstPageable, 15);

      doReturn(firstPageResults)
          .when(recipeSearchRepository)
          .searchByKeyword(eq(keyword), any(Pageable.class));

      Page<RecipeSearch> result = recipeSearchService.search(userId, keyword, 0);

      assertThat(result.getContent()).hasSize(3);
      assertThat(result.getContent().getFirst().getSearchText()).isEqualTo("김치찌개");
      assertThat(result.getTotalElements()).isEqualTo(15);
      assertThat(result.getNumber()).isEqualTo(0);
      verify(recipeSearchRepository).searchByKeyword(keyword, RecipeSearchPageRequest.create(0));
      verify(recipeSearchHistoryService).create(userId, keyword);
    }

    @Test
    @DisplayName("Given - 공백이 포함된 검색어가 주어졌을 때 When - 검색한다면 Then - 검색 결과를 반환해야 한다")
    void givenKeywordWithSpaces_whenSearching_thenShouldReturnSearchResults() {
      UUID userId = UUID.randomUUID();
      String keyword = "김치 찌개 레시피";

      RecipeSearch recipe1 = RecipeSearch.builder().id("1").searchText("김치찌개 맛있는 레시피").build();

      List<RecipeSearch> content = List.of(recipe1);
      Pageable pageable = RecipeSearchPageRequest.create(0);
      Page<RecipeSearch> searchResults = new PageImpl<>(content, pageable, 1);

      doReturn(searchResults)
          .when(recipeSearchRepository)
          .searchByKeyword(eq(keyword), any(Pageable.class));

      Page<RecipeSearch> result = recipeSearchService.search(userId, keyword, 0);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getSearchText()).contains("김치찌개");
      assertThat(result.getContent().getFirst().getSearchText()).contains("레시피");
      verify(recipeSearchRepository).searchByKeyword(keyword, RecipeSearchPageRequest.create(0));
      verify(recipeSearchHistoryService).create(userId, keyword);
    }
  }
}
