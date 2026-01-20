package com.cheftory.api.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.search.history.SearchHistoryService;
import com.cheftory.api.search.query.SearchQueryScope;
import com.cheftory.api.search.query.SearchQueryService;
import com.cheftory.api.search.query.entity.SearchQuery;
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
@DisplayName("SearchFacade Tests")
class SearchFacadeTest {

    @Mock
    private SearchQueryService searchQueryService;

    @Mock
    private SearchHistoryService searchHistoryService;

    @InjectMocks
    private SearchFacade searchFacade;

    @Nested
    @DisplayName("레시피 검색")
    class SearchRecipes {

        @Test
        @DisplayName("Given - 유효한 검색어가 주어졌을 때 When - 첫 페이지를 검색한다면 Then - 검색 결과를 반환해야 한다")
        void givenValidKeyword_whenSearchingFirstPage_thenShouldReturnSearchResults() {
            UUID userId = UUID.randomUUID();
            String keyword = "김치찌개";

            SearchQuery recipe1 =
                    SearchQuery.builder().id("1").searchText("김치찌개").build();
            SearchQuery recipe2 =
                    SearchQuery.builder().id("2").searchText("김치찌개 레시피").build();
            SearchQuery recipe3 =
                    SearchQuery.builder().id("3").searchText("맛있는 김치찌개").build();

            List<SearchQuery> content = List.of(recipe1, recipe2, recipe3);
            Pageable pageable = Pageable.ofSize(10).withPage(0); // pageable 검증은 서비스 단에서 하는게 자연스러움
            Page<SearchQuery> searchResults = new PageImpl<>(content, pageable, 3);

            doReturn(searchResults)
                    .when(searchQueryService)
                    .searchByKeyword(eq(SearchQueryScope.RECIPE), eq(keyword), eq(0));

            Page<SearchQuery> result = searchFacade.search(SearchQueryScope.RECIPE, userId, keyword, 0);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getSearchText()).isEqualTo("김치찌개");
            assertThat(result.getContent().get(1).getSearchText()).isEqualTo("김치찌개 레시피");
            assertThat(result.getContent().get(2).getSearchText()).isEqualTo("맛있는 김치찌개");
            assertThat(result.getTotalElements()).isEqualTo(3);

            verify(searchQueryService).searchByKeyword(SearchQueryScope.RECIPE, keyword, 0);
            verify(searchHistoryService).create(userId, keyword);
        }

        @Test
        @DisplayName("Given - 유효한 검색어가 주어졌을 때 When - 커서로 검색한다면 Then - 커서 결과를 반환해야 한다")
        void givenValidKeyword_whenSearchingWithCursor_thenShouldReturnCursorResults() {
            UUID userId = UUID.randomUUID();
            String keyword = "김치찌개";
            String cursor = "cursor-1";
            String nextCursor = "cursor-2";

            SearchQuery recipe =
                    SearchQuery.builder().id("1").searchText("김치찌개").build();
            CursorPage<SearchQuery> cursorPage = CursorPage.of(List.of(recipe), nextCursor);

            doReturn(cursorPage)
                    .when(searchQueryService)
                    .searchByKeyword(eq(SearchQueryScope.RECIPE), eq(keyword), eq(cursor));

            CursorPage<SearchQuery> result = searchFacade.search(SearchQueryScope.RECIPE, userId, keyword, cursor);

            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursor()).isEqualTo(nextCursor);
            verify(searchQueryService).searchByKeyword(SearchQueryScope.RECIPE, keyword, cursor);
            verify(searchHistoryService).create(userId, keyword);
        }

        @Test
        @DisplayName("Given - 검색 결과가 없는 검색어가 주어졌을 때 When - 검색한다면 Then - 빈 페이지를 반환해야 한다")
        void givenKeywordWithNoResults_whenSearching_thenShouldReturnEmptyPage() {
            UUID userId = UUID.randomUUID();
            String keyword = "존재하지않는검색어";

            Page<SearchQuery> emptyResults = Page.empty();

            doReturn(emptyResults)
                    .when(searchQueryService)
                    .searchByKeyword(eq(SearchQueryScope.RECIPE), eq(keyword), eq(0));

            Page<SearchQuery> result = searchFacade.search(SearchQueryScope.RECIPE, userId, keyword, 0);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(searchQueryService).searchByKeyword(SearchQueryScope.RECIPE, keyword, 0);
            verify(searchHistoryService).create(userId, keyword);
        }

        @Test
        @DisplayName("Given - 여러 페이지의 검색 결과가 있을 때 When - 첫 페이지를 검색한다면 Then - 첫 페이지 결과를 반환해야 한다")
        void givenMultiplePages_whenSearchingFirstPage_thenShouldReturnFirstPageResults() {
            UUID userId = UUID.randomUUID();
            String keyword = "찌개";

            List<SearchQuery> firstPageContent = List.of(
                    SearchQuery.builder().id("1").searchText("김치찌개").build(),
                    SearchQuery.builder().id("2").searchText("된장찌개").build(),
                    SearchQuery.builder().id("3").searchText("부대찌개").build());

            Pageable pageable = Pageable.ofSize(10).withPage(0);
            Page<SearchQuery> firstPageResults = new PageImpl<>(firstPageContent, pageable, 15);

            doReturn(firstPageResults)
                    .when(searchQueryService)
                    .searchByKeyword(eq(SearchQueryScope.RECIPE), eq(keyword), eq(0));

            Page<SearchQuery> result = searchFacade.search(SearchQueryScope.RECIPE, userId, keyword, 0);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().getFirst().getSearchText()).isEqualTo("김치찌개");
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getNumber()).isEqualTo(0);

            verify(searchQueryService).searchByKeyword(SearchQueryScope.RECIPE, keyword, 0);
            verify(searchHistoryService).create(userId, keyword);
        }

        @Test
        @DisplayName("Given - 공백이 포함된 검색어가 주어졌을 때 When - 검색한다면 Then - 검색 결과를 반환해야 한다")
        void givenKeywordWithSpaces_whenSearching_thenShouldReturnSearchResults() {
            UUID userId = UUID.randomUUID();
            String keyword = "김치 찌개 레시피";

            SearchQuery recipe1 =
                    SearchQuery.builder().id("1").searchText("김치찌개 맛있는 레시피").build();

            List<SearchQuery> content = List.of(recipe1);
            Pageable pageable = Pageable.ofSize(10).withPage(0);
            Page<SearchQuery> searchResults = new PageImpl<>(content, pageable, 1);

            doReturn(searchResults)
                    .when(searchQueryService)
                    .searchByKeyword(eq(SearchQueryScope.RECIPE), eq(keyword), eq(0));

            Page<SearchQuery> result = searchFacade.search(SearchQueryScope.RECIPE, userId, keyword, 0);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getSearchText()).contains("김치찌개");
            assertThat(result.getContent().getFirst().getSearchText()).contains("레시피");

            verify(searchQueryService).searchByKeyword(SearchQueryScope.RECIPE, keyword, 0);
            verify(searchHistoryService).create(userId, keyword);
        }

        @Test
        @DisplayName("Given - 공백/빈 문자열 검색어가 주어졌을 때 When - 검색한다면 Then - 히스토리는 저장하지 않는다")
        void givenBlankKeyword_whenSearching_thenShouldNotCreateHistory() {
            UUID userId = UUID.randomUUID();
            String keyword = "   ";

            Page<SearchQuery> emptyResults = Page.empty();
            doReturn(emptyResults)
                    .when(searchQueryService)
                    .searchByKeyword(eq(SearchQueryScope.RECIPE), eq(keyword), eq(0));

            Page<SearchQuery> result = searchFacade.search(SearchQueryScope.RECIPE, userId, keyword, 0);

            assertThat(result.getContent()).isEmpty();

            verify(searchQueryService).searchByKeyword(SearchQueryScope.RECIPE, keyword, 0);
            verify(searchHistoryService, never()).create(any(UUID.class), any(String.class));
        }
    }
}
