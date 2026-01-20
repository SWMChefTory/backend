package com.cheftory.api.search.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
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
@DisplayName("RecipeSearchHistoryController Tests")
public class SearchHistoryControllerTest {

    @Mock
    private SearchHistoryService searchHistoryService;

    @InjectMocks
    private SearchHistoryController searchHistoryController;

    @Nested
    @DisplayName("검색 히스토리 조회")
    class GetSearchHistories {

        @Test
        @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때 When - 검색 히스토리를 조회한다면 Then - 검색 히스토리 목록을 반환해야 한다")
        void givenValidUserId_whenGettingSearchHistories_thenShouldReturnSearchHistories() {
            // Given
            UUID userId = UUID.randomUUID();
            List<String> mockHistories = List.of("김치찌개", "된장찌개", "부대찌개");

            when(searchHistoryService.get(userId, SearchHistoryScope.RECIPE)).thenReturn(mockHistories);

            // When
            SearchHistoriesResponse result =
                    searchHistoryController.getSearchHistories(userId, SearchHistoryScope.RECIPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.histories()).hasSize(3);
            assertThat(result.histories().stream().map(h -> h.history()).toList())
                    .containsExactly("김치찌개", "된장찌개", "부대찌개");
            verify(searchHistoryService).get(userId, SearchHistoryScope.RECIPE);
        }

        @Test
        @DisplayName("Given - 검색 히스토리가 없는 사용자일 때 When - 검색 히스토리를 조회한다면 Then - 빈 목록을 반환해야 한다")
        void givenUserWithNoHistories_whenGettingSearchHistories_thenShouldReturnEmptyList() {
            // Given
            UUID userId = UUID.randomUUID();
            List<String> emptyHistories = List.of();

            when(searchHistoryService.get(userId, SearchHistoryScope.RECIPE)).thenReturn(emptyHistories);

            // When
            SearchHistoriesResponse result =
                    searchHistoryController.getSearchHistories(userId, SearchHistoryScope.RECIPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.histories()).isEmpty();
            verify(searchHistoryService).get(userId, SearchHistoryScope.RECIPE);
        }

        @Test
        @DisplayName("Given - 단일 검색 히스토리가 있는 사용자일 때 When - 검색 히스토리를 조회한다면 Then - 해당 히스토리를 반환해야 한다")
        void givenUserWithSingleHistory_whenGettingSearchHistories_thenShouldReturnSingleHistory() {
            // Given
            UUID userId = UUID.randomUUID();
            List<String> singleHistory = List.of("파스타");

            when(searchHistoryService.get(userId, SearchHistoryScope.RECIPE)).thenReturn(singleHistory);

            // When
            SearchHistoriesResponse result =
                    searchHistoryController.getSearchHistories(userId, SearchHistoryScope.RECIPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.histories()).hasSize(1);
            assertThat(result.histories().get(0).history()).isEqualTo("파스타");
            verify(searchHistoryService).get(userId, SearchHistoryScope.RECIPE);
        }
    }

    @Nested
    @DisplayName("검색 히스토리 삭제")
    class DeleteSearchHistory {

        @Test
        @DisplayName("Given - 유효한 사용자 ID와 검색어가 주어졌을 때 When - 검색 히스토리를 삭제한다면 Then - 성공 응답을 반환해야 한다")
        void givenValidUserIdAndSearchText_whenDeletingSearchHistory_thenShouldReturnSuccessResponse() {
            // Given
            UUID userId = UUID.randomUUID();
            String searchText = "김치찌개";

            // When
            SuccessOnlyResponse result =
                    searchHistoryController.deleteSearchHistory(userId, SearchHistoryScope.RECIPE, searchText);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("success");
            verify(searchHistoryService).delete(userId, SearchHistoryScope.RECIPE, searchText);
        }

        @Test
        @DisplayName("Given - 공백이 포함된 검색어가 주어졌을 때 When - 검색 히스토리를 삭제한다면 Then - 성공 응답을 반환해야 한다")
        void givenSearchTextWithSpaces_whenDeletingSearchHistory_thenShouldReturnSuccessResponse() {
            // Given
            UUID userId = UUID.randomUUID();
            String searchText = "김치 찌개 레시피";

            // When
            SuccessOnlyResponse result =
                    searchHistoryController.deleteSearchHistory(userId, SearchHistoryScope.RECIPE, searchText);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("success");
            verify(searchHistoryService).delete(userId, SearchHistoryScope.RECIPE, searchText);
        }

        @Test
        @DisplayName("Given - 빈 문자열 검색어가 주어졌을 때 When - 검색 히스토리를 삭제한다면 Then - 성공 응답을 반환해야 한다")
        void givenEmptySearchText_whenDeletingSearchHistory_thenShouldReturnSuccessResponse() {
            // Given
            UUID userId = UUID.randomUUID();
            String searchText = "";

            // When
            SuccessOnlyResponse result =
                    searchHistoryController.deleteSearchHistory(userId, SearchHistoryScope.RECIPE, searchText);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("success");
            verify(searchHistoryService).delete(userId, SearchHistoryScope.RECIPE, searchText);
        }

        @Test
        @DisplayName("Given - null 검색어가 주어졌을 때 When - 검색 히스토리를 삭제한다면 Then - 성공 응답을 반환해야 한다")
        void givenNullSearchText_whenDeletingSearchHistory_thenShouldReturnSuccessResponse() {
            // Given
            UUID userId = UUID.randomUUID();
            String searchText = null;

            // When
            SuccessOnlyResponse result =
                    searchHistoryController.deleteSearchHistory(userId, SearchHistoryScope.RECIPE, searchText);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("success");
            verify(searchHistoryService).delete(userId, SearchHistoryScope.RECIPE, searchText);
        }

        @Test
        @DisplayName("Given - 특수문자가 포함된 검색어가 주어졌을 때 When - 검색 히스토리를 삭제한다면 Then - 성공 응답을 반환해야 한다")
        void givenSearchTextWithSpecialCharacters_whenDeletingSearchHistory_thenShouldReturnSuccessResponse() {
            // Given
            UUID userId = UUID.randomUUID();
            String searchText = "김치찌개!@#$%^&*()";

            // When
            SuccessOnlyResponse result =
                    searchHistoryController.deleteSearchHistory(userId, SearchHistoryScope.RECIPE, searchText);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("success");
            verify(searchHistoryService).delete(userId, SearchHistoryScope.RECIPE, searchText);
        }
    }

    @Nested
    @DisplayName("모든 검색 히스토리 삭제")
    class DeleteAllSearchHistories {

        @Test
        @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때 When - 모든 검색 히스토리를 삭제한다면 Then - 성공 응답을 반환해야 한다")
        void givenValidUserId_whenDeletingAllSearchHistories_thenShouldReturnSuccessResponse() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            SuccessOnlyResponse result =
                    searchHistoryController.deleteAllSearchHistories(userId, SearchHistoryScope.RECIPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("success");
            verify(searchHistoryService).deleteAll(userId, SearchHistoryScope.RECIPE);
        }

        @Test
        @DisplayName("Given - 다른 사용자 ID가 주어졌을 때 When - 모든 검색 히스토리를 삭제한다면 Then - 성공 응답을 반환해야 한다")
        void givenDifferentUserId_whenDeletingAllSearchHistories_thenShouldReturnSuccessResponse() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            SuccessOnlyResponse result =
                    searchHistoryController.deleteAllSearchHistories(userId, SearchHistoryScope.RECIPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("success");
            verify(searchHistoryService).deleteAll(userId, SearchHistoryScope.RECIPE);
        }

        @Test
        @DisplayName("Given - 검색 히스토리가 없는 사용자일 때 When - 모든 검색 히스토리를 삭제한다면 Then - 성공 응답을 반환해야 한다")
        void givenUserWithNoHistories_whenDeletingAllSearchHistories_thenShouldReturnSuccessResponse() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            SuccessOnlyResponse result =
                    searchHistoryController.deleteAllSearchHistories(userId, SearchHistoryScope.RECIPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("success");
            verify(searchHistoryService).deleteAll(userId, SearchHistoryScope.RECIPE);
        }
    }
}
