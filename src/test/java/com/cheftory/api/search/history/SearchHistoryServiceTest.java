package com.cheftory.api.search.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeSearchHistoryService Tests")
class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryRepository repository;

    @Mock
    private SearchHistoryKeyGenerator keyGenerator;

    @Mock
    private Clock clock;

    @InjectMocks
    private SearchHistoryService service;

    @Nested
    @DisplayName("create 메서드 테스트")
    class CreateTest {

        @Nested
        @DisplayName("Given - 유효한 사용자 ID와 검색어가 주어졌을 때")
        class GivenValidUserIdAndSearchText {

            private UUID userId;
            private String searchText;
            private String key;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = "김치찌개";
                key = "korea:searchHistory:recipe:" + userId;

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                doNothing().when(repository).save(anyString(), anyString(), any(Clock.class));
                doNothing().when(repository).removeOldEntries(anyString(), anyInt());
                doNothing().when(repository).setExpire(anyString(), any(Duration.class));
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 생성한다면")
            class WhenCreatingSearchHistory {

                @Test
                @DisplayName("Then - 검색어가 저장되어야 한다")
                void thenShouldSaveSearchText() {
                    service.create(userId, searchText);

                    verify(keyGenerator).generate(userId, SearchHistoryScope.RECIPE);
                    verify(repository).save(key, searchText, clock);
                }

                @Test
                @DisplayName("Then - 오래된 항목이 정리되어야 한다")
                void thenShouldRemoveOldEntries() {
                    service.create(userId, searchText);

                    verify(repository).removeOldEntries(key, 10);
                }

                @Test
                @DisplayName("Then - 만료 시간이 설정되어야 한다")
                void thenShouldSetExpireTime() {
                    service.create(userId, searchText);

                    verify(repository).setExpire(key, Duration.ofDays(30));
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 null인 경우")
        class GivenNullSearchText {

            private UUID userId;
            private String searchText;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = null;
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 생성한다면")
            class WhenCreatingSearchHistory {

                @Test
                @DisplayName("Then - 아무것도 저장하지 않아야 한다")
                void thenShouldNotSaveAnything() {
                    service.create(userId, searchText);

                    verify(keyGenerator, never()).generate(any(UUID.class), any(SearchHistoryScope.class));
                    verify(repository, never()).save(anyString(), anyString(), any(Clock.class));
                    verify(repository, never()).removeOldEntries(anyString(), anyInt());
                    verify(repository, never()).setExpire(anyString(), any(Duration.class));
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 빈 문자열인 경우")
        class GivenBlankSearchText {

            private UUID userId;
            private String searchText;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = "   ";
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 생성한다면")
            class WhenCreatingSearchHistory {

                @Test
                @DisplayName("Then - 아무것도 저장하지 않아야 한다")
                void thenShouldNotSaveAnything() {
                    service.create(userId, searchText);

                    verify(keyGenerator, never()).generate(any(UUID.class), any(SearchHistoryScope.class));
                    verify(repository, never()).save(anyString(), anyString(), any(Clock.class));
                    verify(repository, never()).removeOldEntries(anyString(), anyInt());
                    verify(repository, never()).setExpire(anyString(), any(Duration.class));
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어에 공백이 포함된 경우")
        class GivenSearchTextWithWhitespace {

            private UUID userId;
            private String searchText;
            private String trimmedText;
            private String key;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = "  김치찌개  ";
                trimmedText = "김치찌개";
                key = "recipeSearch:history:" + userId;

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                doNothing().when(repository).save(anyString(), anyString(), any(Clock.class));
                doNothing().when(repository).removeOldEntries(anyString(), anyInt());
                doNothing().when(repository).setExpire(anyString(), any(Duration.class));
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 생성한다면")
            class WhenCreatingSearchHistory {

                @Test
                @DisplayName("Then - 공백이 제거된 검색어가 저장되어야 한다")
                void thenShouldSaveTrimmedSearchText() {
                    service.create(userId, searchText);

                    verify(repository).save(key, trimmedText, clock);
                }
            }
        }

        @Nested
        @DisplayName("Given - 특수 문자가 포함된 검색어인 경우")
        class GivenSearchTextWithSpecialCharacters {

            private UUID userId;
            private String searchText;
            private String key;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = "김치찌개!@#$%^&*()";
                key = "recipeSearch:history:" + userId;

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                doNothing().when(repository).save(anyString(), anyString(), any(Clock.class));
                doNothing().when(repository).removeOldEntries(anyString(), anyInt());
                doNothing().when(repository).setExpire(anyString(), any(Duration.class));
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 생성한다면")
            class WhenCreatingSearchHistory {

                @Test
                @DisplayName("Then - 특수 문자가 포함된 검색어가 저장되어야 한다")
                void thenShouldSaveSearchTextWithSpecialCharacters() {
                    service.create(userId, searchText);

                    verify(repository).save(key, searchText, clock);
                }
            }
        }
    }

    @Nested
    @DisplayName("get 메서드 테스트")
    class GetTest {

        @Nested
        @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
        class GivenValidUserId {

            private UUID userId;
            private String key;
            private List<String> expectedHistory;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                key = "recipeSearch:history:" + userId;
                expectedHistory = List.of("김치찌개", "된장찌개", "부대찌개");

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                when(repository.findRecent(key, 10)).thenReturn(expectedHistory);
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 조회한다면")
            class WhenGettingSearchHistory {

                @Test
                @DisplayName("Then - 검색 히스토리 목록을 반환해야 한다")
                void thenShouldReturnSearchHistory() {
                    List<String> result = service.get(userId);

                    assertThat(result).isEqualTo(expectedHistory);
                    verify(keyGenerator).generate(userId, SearchHistoryScope.RECIPE);
                    verify(repository).findRecent(key, 10);
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색 히스토리가 없는 사용자인 경우")
        class GivenUserWithNoHistory {

            private UUID userId;
            private String key;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                key = "recipeSearch:history:" + userId;

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                when(repository.findRecent(key, 10)).thenReturn(List.of());
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 조회한다면")
            class WhenGettingSearchHistory {

                @Test
                @DisplayName("Then - 빈 목록을 반환해야 한다")
                void thenShouldReturnEmptyList() {
                    List<String> result = service.get(userId);

                    assertThat(result).isEmpty();
                    verify(keyGenerator).generate(userId, SearchHistoryScope.RECIPE);
                    verify(repository).findRecent(key, 10);
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색 히스토리가 최대 개수를 초과하는 사용자인 경우")
        class GivenUserWithExcessiveHistory {

            private UUID userId;
            private String key;
            private List<String> expectedHistory;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                key = "recipeSearch:history:" + userId;
                expectedHistory =
                        List.of("검색어1", "검색어2", "검색어3", "검색어4", "검색어5", "검색어6", "검색어7", "검색어8", "검색어9", "검색어10");

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                when(repository.findRecent(key, 10)).thenReturn(expectedHistory);
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 조회한다면")
            class WhenGettingSearchHistory {

                @Test
                @DisplayName("Then - 최대 10개의 최근 검색어를 반환해야 한다")
                void thenShouldReturnMaxRecentSearchHistory() {
                    List<String> result = service.get(userId);

                    assertThat(result).hasSize(10);
                    assertThat(result).isEqualTo(expectedHistory);
                    verify(repository).findRecent(key, 10);
                }
            }
        }
    }

    @Nested
    @DisplayName("delete 메서드 테스트")
    class DeleteTest {

        @Nested
        @DisplayName("Given - 유효한 사용자 ID와 검색어가 주어졌을 때")
        class GivenValidUserIdAndSearchText {

            private UUID userId;
            private String searchText;
            private String key;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = "김치찌개";
                key = "recipeSearch:history:" + userId;

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                doNothing().when(repository).remove(anyString(), anyString());
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 삭제한다면")
            class WhenDeletingSearchHistory {

                @Test
                @DisplayName("Then - 해당 검색어가 삭제되어야 한다")
                void thenShouldDeleteSearchText() {
                    service.delete(userId, searchText);

                    verify(keyGenerator).generate(userId, SearchHistoryScope.RECIPE);
                    verify(repository).remove(key, searchText);
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 null인 경우")
        class GivenNullSearchText {

            private UUID userId;
            private String searchText;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = null;
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 삭제한다면")
            class WhenDeletingSearchHistory {

                @Test
                @DisplayName("Then - 아무것도 삭제하지 않아야 한다")
                void thenShouldNotDeleteAnything() {
                    service.delete(userId, searchText);

                    verify(keyGenerator, never()).generate(any(UUID.class), any(SearchHistoryScope.class));
                    verify(repository, never()).remove(anyString(), anyString());
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 빈 문자열인 경우")
        class GivenBlankSearchText {

            private UUID userId;
            private String searchText;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = "   ";
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 삭제한다면")
            class WhenDeletingSearchHistory {

                @Test
                @DisplayName("Then - 아무것도 삭제하지 않아야 한다")
                void thenShouldNotDeleteAnything() {
                    service.delete(userId, searchText);

                    verify(keyGenerator, never()).generate(any(UUID.class), any(SearchHistoryScope.class));
                    verify(repository, never()).remove(anyString(), anyString());
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어에 공백이 포함된 경우")
        class GivenSearchTextWithWhitespace {

            private UUID userId;
            private String searchText;
            private String trimmedText;
            private String key;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = "  김치찌개  ";
                trimmedText = "김치찌개";
                key = "recipeSearch:history:" + userId;

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                doNothing().when(repository).remove(anyString(), anyString());
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 삭제한다면")
            class WhenDeletingSearchHistory {

                @Test
                @DisplayName("Then - 공백이 제거된 검색어가 삭제되어야 한다")
                void thenShouldDeleteTrimmedSearchText() {
                    service.delete(userId, searchText);

                    verify(repository).remove(key, trimmedText);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 검색어인 경우")
        class GivenNonExistentSearchText {

            private UUID userId;
            private String searchText;
            private String key;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                searchText = "존재하지않는검색어";
                key = "recipeSearch:history:" + userId;

                when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
                doNothing().when(repository).remove(anyString(), anyString());
            }

            @Nested
            @DisplayName("When - 검색 히스토리를 삭제한다면")
            class WhenDeletingSearchHistory {

                @Test
                @DisplayName("Then - 삭제 작업이 수행되어야 한다")
                void thenShouldPerformDeleteOperation() {
                    service.delete(userId, searchText);

                    verify(repository).remove(key, searchText);
                }
            }
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("전체 검색 히스토리 관리 시나리오를 테스트할 수 있다")
        void shouldHandleCompleteSearchHistoryManagementScenario() {
            UUID userId = UUID.randomUUID();
            String key = "recipeSearch:history:" + userId;
            String searchText1 = "김치찌개";
            String searchText2 = "된장찌개";
            String searchText3 = "부대찌개";

            when(keyGenerator.generate(userId, SearchHistoryScope.RECIPE)).thenReturn(key);
            doNothing().when(repository).save(anyString(), anyString(), any(Clock.class));
            doNothing().when(repository).removeOldEntries(anyString(), anyInt());
            doNothing().when(repository).setExpire(anyString(), any(Duration.class));
            doNothing().when(repository).remove(anyString(), anyString());

            service.create(userId, searchText1);
            service.create(userId, searchText2);
            service.create(userId, searchText3);

            verify(repository, times(3)).save(eq(key), anyString(), eq(clock));
            verify(repository, times(3)).removeOldEntries(eq(key), eq(10));
            verify(repository, times(3)).setExpire(eq(key), eq(Duration.ofDays(30)));

            service.delete(userId, searchText2);

            verify(repository).remove(key, searchText2);
        }

        @Test
        @DisplayName("빈 검색어는 저장되지 않는다")
        void shouldNotSaveEmptySearchTexts() {
            UUID userId = UUID.randomUUID();

            service.create(userId, null);
            service.create(userId, "");
            service.create(userId, "   ");

            verify(keyGenerator, never()).generate(any(UUID.class), any(SearchHistoryScope.class));
            verify(repository, never()).save(anyString(), anyString(), any(Clock.class));
        }
    }
}
