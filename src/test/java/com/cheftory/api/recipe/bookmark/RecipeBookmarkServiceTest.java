package com.cheftory.api.recipe.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.bookmark.repository.RecipeBookmarkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeBookmarkService Tests")
public class RecipeBookmarkServiceTest {

    private RecipeBookmarkRepository repository;
    private RecipeBookmarkService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeBookmarkRepository.class);
        clock = mock(Clock.class);
        service = new RecipeBookmarkService(repository, clock);
    }

    @Nested
    @DisplayName("레시피 북마크 생성")
    class CreateRecipeBookmark {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {

            private UUID recipeId;
            private UUID userId;
            private LocalDateTime fixedTime;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
                fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                doReturn(fixedTime).when(clock).now();
            }

            @Test
            @DisplayName("Then - create가 호출되고 true를 반환해야 한다")
            void thenShouldCallCreateAndReturnTrue() {
                doReturn(true).when(repository).create(any(RecipeBookmark.class));

                boolean result = service.create(userId, recipeId);

                assertThat(result).isTrue();

                verify(repository)
                        .create(argThat(h -> h.getRecipeId().equals(recipeId)
                                && h.getUserId().equals(userId)));

                verify(repository, never()).recreate(any(), any(), any());
            }
        }

        @Nested
        @DisplayName("Given - 이미 북마크가 있을 때(중복 저장 상황)")
        class GivenExistingRecipeBookmark {

            private UUID recipeId;
            private UUID userId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
            }

            @Test
            void create_duplicate_existingActive_returnsTrue() {
                doThrow(new RecipeBookmarkException(
                                com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode
                                        .RECIPE_BOOKMARK_ALREADY_EXISTS))
                        .when(repository)
                        .create(any(RecipeBookmark.class));

                doReturn(true).when(repository).recreate(userId, recipeId, clock);

                boolean result = service.create(userId, recipeId);

                assertTrue(result);

                verify(repository).create(any(RecipeBookmark.class));
                verify(repository).recreate(userId, recipeId, clock);
            }

            @Test
            void create_duplicate_recreateFails_propagatesException() {
                RecipeBookmarkException exception = new RecipeBookmarkException(
                        com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
                doThrow(exception).when(repository).create(any(RecipeBookmark.class));

                doThrow(exception).when(repository).recreate(userId, recipeId, clock);

                RecipeBookmarkException thrown =
                        assertThrows(RecipeBookmarkException.class, () -> service.create(userId, recipeId));

                assertSame(exception, thrown);

                verify(repository).create(any(RecipeBookmark.class));
                verify(repository).recreate(userId, recipeId, clock);
            }
        }
    }

    @Nested
    @DisplayName("커서 기반 조회")
    class CursorQueries {

        @Test
        @DisplayName("cursor가 비어 있으면 최근 북마크 첫 페이지를 조회한다")
        void shouldGetRecentsFirstPageWithCursor() {
            UUID userId = UUID.randomUUID();
            List<RecipeBookmark> bookmarks = List.of(mock(RecipeBookmark.class), mock(RecipeBookmark.class));

            CursorPage<RecipeBookmark> cursorPage = new CursorPage<>(bookmarks, "next-cursor");
            doReturn(cursorPage).when(repository).keysetRecentsFirst(userId);

            CursorPage<RecipeBookmark> result = service.getRecents(userId, null);

            assertThat(result.items()).hasSize(2);
            assertThat(result.nextCursor()).isEqualTo("next-cursor");

            verify(repository).keysetRecentsFirst(userId);
        }

        @Test
        @DisplayName("cursor가 있으면 최근 북마크 keyset을 조회한다")
        void shouldGetRecentsKeysetWithCursor() {
            UUID userId = UUID.randomUUID();
            String cursor = "cursor";

            List<RecipeBookmark> bookmarks = List.of(mock(RecipeBookmark.class), mock(RecipeBookmark.class));
            CursorPage<RecipeBookmark> cursorPage = new CursorPage<>(bookmarks, "next-cursor");
            doReturn(cursorPage).when(repository).keysetRecents(userId, cursor);

            CursorPage<RecipeBookmark> result = service.getRecents(userId, cursor);

            assertThat(result.items()).hasSize(2);
            verify(repository).keysetRecents(userId, cursor);
        }

        @Test
        @DisplayName("커서 기반 카테고리 조회는 keyset을 사용한다")
        void shouldGetCategorizedKeysetWithCursor() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            String cursor = "cursor";

            List<RecipeBookmark> bookmarks = List.of(mock(RecipeBookmark.class), mock(RecipeBookmark.class));
            CursorPage<RecipeBookmark> cursorPage = new CursorPage<>(bookmarks, "next-cursor");

            doReturn(cursorPage).when(repository).keysetCategorized(userId, categoryId, cursor);

            CursorPage<RecipeBookmark> result = service.getCategorized(userId, categoryId, cursor);

            assertThat(result.items()).hasSize(2);
            verify(repository).keysetCategorized(userId, categoryId, cursor);
        }

        @Nested
        @DisplayName("레시피 북마크 조회")
        class FindRecipeBookmark {

            @Nested
            @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
            class GivenValidRecipeAndUserId {

                private UUID recipeId;
                private UUID userId;

                @BeforeEach
                void setUp() {
                    recipeId = UUID.randomUUID();
                    userId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 레시피 북마크를 조회한다면")
                class WhenFindingRecipeBookmark {

                    private RecipeBookmark realRecipeBookmark;

                    @BeforeEach
                    void beforeEach() {
                        realRecipeBookmark = mock(RecipeBookmark.class);
                        doReturn(realRecipeBookmark).when(repository).get(userId, recipeId);
                    }

                    @Test
                    @DisplayName("Then - get 메서드는 레시피 북마크를 반환해야 한다")
                    public void thenShouldReturnCorrectRecipeBookmark() {
                        RecipeBookmark status = service.get(userId, recipeId);

                        assertThat(status).isNotNull();
                        assertThat(status).isEqualTo(realRecipeBookmark);

                        verify(repository).get(userId, recipeId);
                    }
                }
            }
        }

        @Nested
        @DisplayName("레시피 북마크 카테고리 변경")
        class ChangeRecipeBookmarkRecipeCategory {

            @Nested
            @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
            class GivenValidRecipeAndUserId {

                private UUID recipeId;
                private UUID userId;
                private UUID newCategoryId;

                @BeforeEach
                void setUp() {
                    recipeId = UUID.randomUUID();
                    userId = UUID.randomUUID();
                    newCategoryId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 레시피 북마크의 카테고리를 변경한다면")
                class WhenChangingRecipeBookmarkRecipeCategory {

                    @Test
                    @DisplayName("Then - 카테고리가 올바르게 변경되어야 한다")
                    public void thenShouldChangeCategoryCorrectly() {
                        service.categorize(userId, recipeId, newCategoryId);

                        verify(repository).categorize(userId, recipeId, newCategoryId);
                    }
                }
            }
        }

        @Nested
        @DisplayName("레시피 북마크 카테고리 삭제")
        class DeleteRecipeBookmarkRecipeCategory {

            @Nested
            @DisplayName("Given - 삭제할 카테고리 ID가 주어졌을 때")
            class GivenRecipeCategoryIdToDelete {

                private UUID categoryId;

                @BeforeEach
                void setUp() {
                    categoryId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 해당 카테고리를 가진 레시피 북마크들이 존재한다면")
                class WhenRecipeBookmarksWithRecipeCategoryExist {

                    @Test
                    @DisplayName("Then - 카테고리를 가진 모든 레시피 북마크의 카테고리가 비워져야 한다")
                    public void thenShouldEmptyAllRecipeBookmarkCategories() {
                        service.unCategorize(categoryId);

                        verify(repository).unCategorize(categoryId);
                    }
                }
            }
        }

        @Nested
        @DisplayName("카테고리별 레시피 개수 조회")
        class CountRecipeBookmarksByCategories {

            @Nested
            @DisplayName("Given - 유효한 카테고리 ID 목록이 주어졌을 때")
            class GivenValidRecipeCategoryIds {

                private List<UUID> categoryIds;

                @BeforeEach
                void setUp() {
                    categoryIds = List.of(UUID.randomUUID(), UUID.randomUUID());
                }

                @Nested
                @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
                class WhenCountingRecipeBookmarksByCategories {

                    private List<RecipeBookmarkCategorizedCountProjection> projections;

                    @BeforeEach
                    void beforeEach() {
                        projections = List.of(
                                createMockProjection(categoryIds.get(0), 5L),
                                createMockProjection(categoryIds.get(1), 3L));
                        doReturn(projections).when(repository).countCategorized(categoryIds);
                    }

                    private RecipeBookmarkCategorizedCountProjection createMockProjection(UUID categoryId, Long count) {
                        RecipeBookmarkCategorizedCountProjection projection =
                                mock(RecipeBookmarkCategorizedCountProjection.class);
                        doReturn(categoryId).when(projection).getCategoryId();
                        doReturn(count).when(projection).getCount();
                        return projection;
                    }

                    @Test
                    @DisplayName("Then - 각 카테고리별 레시피 개수가 반환되어야 한다")
                    void thenShouldReturnRecipeBookmarkCountsByCategories() {
                        List<RecipeBookmarkCategorizedCount> result = service.countByCategories(categoryIds);

                        assertThat(result).hasSize(2);

                        RecipeBookmarkCategorizedCount first = result.get(0);
                        assertThat(first.getCategoryId()).isEqualTo(categoryIds.get(0));
                        assertThat(first.getCount()).isEqualTo(5);

                        RecipeBookmarkCategorizedCount second = result.get(1);
                        assertThat(second.getCategoryId()).isEqualTo(categoryIds.get(1));
                        assertThat(second.getCount()).isEqualTo(3);

                        verify(repository).countCategorized(categoryIds);
                    }
                }
            }
        }

        @Nested
        @DisplayName("사용자의 레시피 북마크 목록 조회")
        class GetUserRecipeBookmarks {

            @Nested
            @DisplayName("Given - 유효한 레시피 ID 목록과 사용자 ID가 주어졌을 때")
            class GivenValidRecipeIdsAndUserId {

                private List<UUID> recipeIds;
                private UUID userId;

                @BeforeEach
                void setUp() {
                    recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
                    userId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 사용자의 레시피 북마크 목록을 조회한다면")
                class WhenGettingUserRecipeBookmarks {

                    private List<RecipeBookmark> viewStatuses;

                    @BeforeEach
                    void beforeEach() {
                        viewStatuses = List.of(mock(RecipeBookmark.class), mock(RecipeBookmark.class));

                        doReturn(viewStatuses).when(repository).gets(userId, recipeIds);
                    }

                    @Test
                    @DisplayName("Then - 사용자가 조회한 레시피 상태 목록을 반환해야 한다")
                    void thenShouldReturnUserRecipeBookmarks() {
                        List<RecipeBookmark> result = service.gets(recipeIds, userId);

                        assertThat(result).hasSize(2);
                        assertThat(result).containsExactlyElementsOf(viewStatuses);
                        verify(repository).gets(userId, recipeIds);
                    }
                }
            }

            @Nested
            @DisplayName("Given - 사용자가 조회하지 않은 레시피 ID 목록이 주어졌을 때")
            class GivenUnviewedRecipeIds {

                private List<UUID> recipeIds;
                private UUID userId;

                @BeforeEach
                void setUp() {
                    recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
                    userId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 사용자의 레시피 북마크 목록을 조회한다면")
                class WhenGettingUserRecipeBookmarks {

                    @BeforeEach
                    void beforeEach() {
                        doReturn(List.of()).when(repository).gets(userId, recipeIds);
                    }

                    @Test
                    @DisplayName("Then - 빈 목록을 반환해야 한다")
                    void thenShouldReturnEmptyList() {
                        List<RecipeBookmark> result = service.gets(recipeIds, userId);

                        assertThat(result).isEmpty();
                        verify(repository).gets(userId, recipeIds);
                    }
                }
            }

            @Nested
            @DisplayName("Given - 빈 레시피 ID 목록이 주어졌을 때")
            class GivenEmptyRecipeIds {

                private UUID userId;

                @BeforeEach
                void setUp() {
                    userId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 사용자의 레시피 북마크 목록을 조회한다면")
                class WhenGettingUserRecipeBookmarks {

                    @BeforeEach
                    void beforeEach() {
                        doReturn(List.of()).when(repository).gets(userId, List.of());
                    }

                    @Test
                    @DisplayName("Then - 빈 목록을 반환해야 한다")
                    void thenShouldReturnEmptyList() {
                        List<RecipeBookmark> result = service.gets(List.of(), userId);

                        assertThat(result).isEmpty();
                        verify(repository).gets(userId, List.of());
                    }
                }
            }

            @Nested
            @DisplayName("Given - 일부만 조회한 레시피 ID 목록이 주어졌을 때")
            class GivenPartiallyViewedRecipeIds {

                private List<UUID> recipeIds;
                private UUID userId;

                @BeforeEach
                void setUp() {
                    recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
                    userId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 사용자의 레시피 북마크 목록을 조회한다면")
                class WhenGettingUserRecipeBookmarks {

                    private List<RecipeBookmark> viewStatuses;
                    private RecipeBookmark bookmark0;
                    private RecipeBookmark bookmark2;

                    @BeforeEach
                    void beforeEach() {
                        bookmark0 = mock(RecipeBookmark.class);
                        bookmark2 = mock(RecipeBookmark.class);
                        viewStatuses = List.of(bookmark0, bookmark2);

                        doReturn(viewStatuses).when(repository).gets(userId, recipeIds);
                    }

                    @Test
                    @DisplayName("Then - 조회한 레시피 상태만 반환해야 한다")
                    void thenShouldReturnOnlyViewedRecipeStatuses() {
                        List<RecipeBookmark> result = service.gets(recipeIds, userId);

                        assertThat(result).hasSize(2);
                        assertThat(result.get(0)).isEqualTo(bookmark0);
                        assertThat(result.get(1)).isEqualTo(bookmark2);
                        verify(repository).gets(userId, recipeIds);
                    }
                }
            }
        }

        @Nested
        @DisplayName("레시피 북마크 삭제")
        class DeleteRecipeBookmark {

            @Nested
            @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
            class GivenValidRecipeAndUserId {

                private UUID recipeId;
                private UUID userId;

                @BeforeEach
                void setUp() {
                    recipeId = UUID.randomUUID();
                    userId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 레시피 북마크를 삭제한다면")
                class WhenDeletingRecipeBookmark {

                    @Test
                    @DisplayName("Then - 레시피 북마크가 삭제되어야 한다")
                    public void thenShouldDeleteRecipeBookmark() {
                        service.delete(userId, recipeId);

                        verify(repository).delete(userId, recipeId, clock);
                    }
                }
            }
        }

        @Nested
        @DisplayName("미분류 레시피 개수 조회")
        class CountUncategorized {

            @Nested
            @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
            class GivenValidUserId {

                private UUID userId;

                @BeforeEach
                void setUp() {
                    userId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 미분류 레시피 개수를 조회한다면")
                class WhenCountingUncategorized {

                    private RecipeBookmarkUnCategorizedCountProjection projection;

                    @BeforeEach
                    void beforeEach() {
                        projection = mock(RecipeBookmarkUnCategorizedCountProjection.class);
                        doReturn(5L).when(projection).getCount();
                        doReturn(projection).when(repository).countUncategorized(userId);
                    }

                    @Test
                    @DisplayName("Then - 올바른 미분류 레시피 개수를 반환해야 한다")
                    void thenShouldReturnCorrectUncategorizedCount() {
                        RecipeBookmarkUnCategorizedCount result = service.countUncategorized(userId);

                        assertThat(result.getCount()).isEqualTo(5);
                        verify(repository).countUncategorized(userId);
                    }
                }
            }

            @Nested
            @DisplayName("Given - 미분류 레시피가 없는 사용자 ID가 주어졌을 때")
            class GivenUserIdWithNoUncategorized {

                private UUID userId;

                @BeforeEach
                void setUp() {
                    userId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 미분류 레시피 개수를 조회한다면")
                class WhenCountingUncategorized {

                    private RecipeBookmarkUnCategorizedCountProjection projection;

                    @BeforeEach
                    void beforeEach() {
                        projection = mock(RecipeBookmarkUnCategorizedCountProjection.class);
                        doReturn(0L).when(projection).getCount();
                        doReturn(projection).when(repository).countUncategorized(userId);
                    }

                    @Test
                    @DisplayName("Then - 0을 반환해야 한다")
                    void thenShouldReturnZero() {
                        RecipeBookmarkUnCategorizedCount result = service.countUncategorized(userId);

                        assertThat(result.getCount()).isEqualTo(0);
                        verify(repository).countUncategorized(userId);
                    }
                }
            }
        }

        @Nested
        @DisplayName("북마크 ID들로 삭제")
        class DeletesByIds {

            @Nested
            @DisplayName("Given - 유효한 북마크 ID 목록이 주어졌을 때")
            class GivenValidBookmarkIds {

                private List<UUID> bookmarkIds;

                @BeforeEach
                void setUp() {
                    bookmarkIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
                }

                @Nested
                @DisplayName("When - 해당 북마크 ID들의 북마크를 삭제한다면")
                class WhenDeletingBookmarksByIds {

                    @Test
                    @DisplayName("Then - 모든 북마크가 삭제되어야 한다")
                    void thenShouldDeleteBookmarksByIds() {
                        service.deletes(bookmarkIds);

                        verify(repository).deletes(bookmarkIds, clock);
                    }
                }
            }
        }

        @Nested
        @DisplayName("레시피별 북마크 차단")
        class BlockByRecipe {

            @Nested
            @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
            class GivenValidRecipeId {

                private UUID recipeId;

                @BeforeEach
                void setUp() {
                    recipeId = UUID.randomUUID();
                }

                @Nested
                @DisplayName("When - 해당 레시피의 모든 북마크를 차단한다면")
                class WhenBlockingAllBookmarksByRecipe {

                    @Test
                    @DisplayName("Then - 모든 북마크가 BLOCKED 상태로 변경되어야 한다")
                    void thenShouldBlockAllBookmarks() {
                        service.block(recipeId);

                        verify(repository).block(recipeId, clock);
                    }
                }
            }
        }
    }
}
