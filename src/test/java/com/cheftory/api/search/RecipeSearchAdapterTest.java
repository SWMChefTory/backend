package com.cheftory.api.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchQueryScope;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeSearchAdapter 테스트")
class RecipeSearchAdapterTest {

    private SearchFacade searchFacade;
    private RecipeSearchAdapter adapter;

    @BeforeEach
    void setUp() {
        searchFacade = mock(SearchFacade.class);
        adapter = new RecipeSearchAdapter(searchFacade);
    }

    @Nested
    @DisplayName("레시피 ID 검색 (searchRecipeIds)")
    class SearchRecipeIds {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            UUID userId;
            String query;
            String cursor;
            SearchQuery searchQuery1;
            SearchQuery searchQuery2;

            @BeforeEach
            void setUp() throws SearchException {
                userId = UUID.randomUUID();
                query = "파스타";
                cursor = null;
                UUID recipeId1 = UUID.randomUUID();
                UUID recipeId2 = UUID.randomUUID();

                searchQuery1 = mock(SearchQuery.class);
                searchQuery2 = mock(SearchQuery.class);
                doReturn(recipeId1.toString()).when(searchQuery1).getId();
                doReturn(recipeId2.toString()).when(searchQuery2).getId();

                CursorPage<SearchQuery> searchPage =
                        new CursorPage<>(List.of(searchQuery1, searchQuery2), "next-cursor");
                doReturn(searchPage).when(searchFacade).search(SearchQueryScope.RECIPE, userId, query, cursor);
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {
                CursorPage<UUID> result;

                @BeforeEach
                void setUp() throws SearchException {
                    result = adapter.searchRecipeIds(userId, query, cursor);
                }

                @Test
                @DisplayName("Then - 레시피 ID 목록을 반환한다")
                void thenReturnsRecipeIds() throws SearchException {
                    assertThat(result.items()).hasSize(2);
                    assertThat(result.nextCursor()).isEqualTo("next-cursor");
                    verify(searchFacade).search(SearchQueryScope.RECIPE, userId, query, cursor);
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색 결과가 없을 때")
        class GivenNoResults {
            UUID userId;
            String query;
            String cursor;

            @BeforeEach
            void setUp() throws SearchException {
                userId = UUID.randomUUID();
                query = "없는레시피";
                cursor = null;

                CursorPage<SearchQuery> emptyPage = new CursorPage<>(List.of(), null);
                doReturn(emptyPage).when(searchFacade).search(SearchQueryScope.RECIPE, userId, query, cursor);
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {
                CursorPage<UUID> result;

                @BeforeEach
                void setUp() throws SearchException {
                    result = adapter.searchRecipeIds(userId, query, cursor);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmpty() {
                    assertThat(result.items()).isEmpty();
                    assertThat(result.nextCursor()).isNull();
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색 예외 발생 시")
        class GivenSearchException {
            UUID userId;
            String query;
            String cursor;
            SearchException exception;

            @BeforeEach
            void setUp() throws SearchException {
                userId = UUID.randomUUID();
                query = "파스타";
                cursor = null;
                exception = mock(SearchException.class);

                doThrow(exception).when(searchFacade).search(SearchQueryScope.RECIPE, userId, query, cursor);
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {

                @Test
                @DisplayName("Then - 예외를 전파한다")
                void thenPropagatesException() {
                    SearchException thrown =
                            assertThrows(SearchException.class, () -> adapter.searchRecipeIds(userId, query, cursor));
                    assertSame(exception, thrown);
                }
            }
        }
    }
}
