package com.cheftory.api.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.search.exception.SearchException;
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
        @DisplayName("Given - 유효한 검색어가 주어졌을 때 When - 커서로 검색한다면 Then - 커서 결과를 반환해야 한다")
        void givenValidKeyword_whenSearchingWithCursor_thenShouldReturnCursorResults() throws SearchException {
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
        @DisplayName("Given - 공백/빈 문자열 검색어가 주어졌을 때 When - 검색한다면 Then - 히스토리는 저장하지 않는다")
        void givenBlankKeyword_whenSearching_thenShouldNotCreateHistory() throws SearchException {
            UUID userId = UUID.randomUUID();
            String keyword = "   ";
            String cursor = null;

            CursorPage<SearchQuery> emptyCursorPage = CursorPage.of(List.of(), null);
            doReturn(emptyCursorPage)
                    .when(searchQueryService)
                    .searchByKeyword(eq(SearchQueryScope.RECIPE), eq(keyword), eq(cursor));

            CursorPage<SearchQuery> result = searchFacade.search(SearchQueryScope.RECIPE, userId, keyword, cursor);

            assertThat(result.items()).isEmpty();

            verify(searchQueryService).searchByKeyword(SearchQueryScope.RECIPE, keyword, cursor);
            verify(searchHistoryService, never()).create(any(UUID.class), any(String.class));
        }
    }
}
