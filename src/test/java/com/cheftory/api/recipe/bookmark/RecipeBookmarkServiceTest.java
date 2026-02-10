package com.cheftory.api.recipe.bookmark;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.bookmark.repository.RecipeBookmarkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;

@DisplayName("RecipeBookmarkService 테스트")
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
    @DisplayName("북마크 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            UUID recipeId;
            UUID userId;
            LocalDateTime fixedTime;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
                fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                doReturn(fixedTime).when(clock).now();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                boolean result;

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    doReturn(true).when(repository).create(any(RecipeBookmark.class));
                    result = service.create(userId, recipeId);
                }

                @Test
                @DisplayName("Then - true를 반환하고 저장한다")
                void thenReturnsTrue() throws RecipeBookmarkException {
                    assertThat(result).isTrue();
                    verify(repository)
                            .create(argThat(h -> h.getRecipeId().equals(recipeId)
                                    && h.getUserId().equals(userId)));
                    verify(repository, never()).recreate(any(), any(), any());
                }
            }
        }

        @Nested
        @DisplayName("Given - 이미 존재하는 북마크일 때")
        class GivenExistingBookmark {
            UUID recipeId;
            UUID userId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 중복 생성 시도 시 재생성 성공하면")
            class WhenRecreateSucceeds {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    doThrow(new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_ALREADY_EXISTS))
                            .when(repository)
                            .create(any(RecipeBookmark.class));
                    doReturn(true).when(repository).recreate(userId, recipeId, clock);
                }

                @Test
                @DisplayName("Then - true를 반환한다")
                void thenReturnsTrue() throws RecipeBookmarkException {
                    boolean result = service.create(userId, recipeId);
                    assertTrue(result);
                    verify(repository).create(any(RecipeBookmark.class));
                    verify(repository).recreate(userId, recipeId, clock);
                }
            }

            @Nested
            @DisplayName("When - 중복 생성 시도 시 재생성 실패하면")
            class WhenRecreateFails {
                RecipeBookmarkException exception;

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    exception = new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
                    doThrow(exception).when(repository).create(any(RecipeBookmark.class));
                    doThrow(exception).when(repository).recreate(userId, recipeId, clock);
                }

                @Test
                @DisplayName("Then - 예외를 전파한다")
                void thenPropagatesException() throws RecipeBookmarkException {
                    RecipeBookmarkException thrown =
                            assertThrows(RecipeBookmarkException.class, () -> service.create(userId, recipeId));
                    assertSame(exception, thrown);
                    verify(repository).create(any(RecipeBookmark.class));
                    verify(repository).recreate(userId, recipeId, clock);
                }
            }
        }
    }

    @Nested
    @DisplayName("커서 기반 조회 (getRecents, getCategorized)")
    class CursorQueries {

        @Nested
        @DisplayName("Given - 최근 북마크 조회 시")
        class GivenRecents {
            UUID userId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 커서가 없으면")
            class WhenNoCursor {
                CursorPage<RecipeBookmark> result;

                @BeforeEach
                void setUp() throws CursorException {
                    List<RecipeBookmark> bookmarks = List.of(mock(RecipeBookmark.class), mock(RecipeBookmark.class));
                    CursorPage<RecipeBookmark> cursorPage = new CursorPage<>(bookmarks, "next-cursor");
                    doReturn(cursorPage).when(repository).keysetRecentsFirst(userId);
                    result = service.getRecents(userId, null);
                }

                @Test
                @DisplayName("Then - 첫 페이지를 조회한다")
                void thenReturnsFirstPage() throws CursorException {
                    assertThat(result.items()).hasSize(2);
                    assertThat(result.nextCursor()).isEqualTo("next-cursor");
                    verify(repository).keysetRecentsFirst(userId);
                }
            }

            @Nested
            @DisplayName("When - 커서가 있으면")
            class WhenCursorExists {
                CursorPage<RecipeBookmark> result;
                String cursor = "cursor";

                @BeforeEach
                void setUp() throws CursorException {
                    List<RecipeBookmark> bookmarks = List.of(mock(RecipeBookmark.class), mock(RecipeBookmark.class));
                    CursorPage<RecipeBookmark> cursorPage = new CursorPage<>(bookmarks, "next-cursor");
                    doReturn(cursorPage).when(repository).keysetRecents(userId, cursor);
                    result = service.getRecents(userId, cursor);
                }

                @Test
                @DisplayName("Then - 해당 커서 이후를 조회한다")
                void thenReturnsNextPage() throws CursorException {
                    assertThat(result.items()).hasSize(2);
                    verify(repository).keysetRecents(userId, cursor);
                }
            }
        }

        @Nested
        @DisplayName("Given - 카테고리별 조회 시")
        class GivenCategorized {
            UUID userId;
            UUID categoryId;
            String cursor;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                cursor = "cursor";
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                CursorPage<RecipeBookmark> result;

                @BeforeEach
                void setUp() throws CursorException {
                    List<RecipeBookmark> bookmarks = List.of(mock(RecipeBookmark.class), mock(RecipeBookmark.class));
                    CursorPage<RecipeBookmark> cursorPage = new CursorPage<>(bookmarks, "next-cursor");
                    doReturn(cursorPage).when(repository).keysetCategorized(userId, categoryId, cursor);
                    result = service.getCategorized(userId, categoryId, cursor);
                }

                @Test
                @DisplayName("Then - 해당 카테고리의 북마크를 조회한다")
                void thenReturnsCategorized() throws CursorException {
                    assertThat(result.items()).hasSize(2);
                    verify(repository).keysetCategorized(userId, categoryId, cursor);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 조회 (get)")
    class Get {

        @Nested
        @DisplayName("Given - 유효한 ID들이 주어졌을 때")
        class GivenValidIds {
            UUID recipeId;
            UUID userId;
            RecipeBookmark bookmark;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
                bookmark = mock(RecipeBookmark.class);
                doReturn(bookmark).when(repository).find(userId, recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                RecipeBookmark result;

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    result = service.get(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 북마크를 반환한다")
                void thenReturnsBookmark() throws RecipeBookmarkException {
                    assertThat(result).isEqualTo(bookmark);
                    verify(repository).find(userId, recipeId);
                }
            }
        }
    }

    @Nested
    @DisplayName("카테고리 변경 (categorize)")
    class Categorize {

        @Nested
        @DisplayName("Given - 유효한 ID들이 주어졌을 때")
        class GivenValidIds {
            UUID recipeId;
            UUID userId;
            UUID newCategoryId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
                newCategoryId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 변경을 요청하면")
            class WhenCategorizing {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    service.categorize(userId, recipeId, newCategoryId);
                }

                @Test
                @DisplayName("Then - 카테고리를 변경한다")
                void thenCategorizes() throws RecipeBookmarkException {
                    verify(repository).categorize(userId, recipeId, newCategoryId);
                }
            }
        }
    }

    @Nested
    @DisplayName("카테고리 해제 (unCategorize)")
    class UnCategorize {

        @Nested
        @DisplayName("Given - 삭제할 카테고리 ID가 주어졌을 때")
        class GivenCategoryId {
            UUID categoryId;

            @BeforeEach
            void setUp() {
                categoryId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 해제를 요청하면")
            class WhenUnCategorizing {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    service.unCategorize(categoryId);
                }

                @Test
                @DisplayName("Then - 해당 카테고리의 북마크들을 해제한다")
                void thenUnCategorizes() throws RecipeBookmarkException {
                    verify(repository).unCategorize(categoryId);
                }
            }
        }
    }

    @Nested
    @DisplayName("카테고리별 개수 조회 (countByCategories)")
    class CountByCategories {

        @Nested
        @DisplayName("Given - 카테고리 ID 목록이 주어졌을 때")
        class GivenCategoryIds {
            List<UUID> categoryIds;
            List<RecipeBookmarkCategorizedCountProjection> projections;

            @BeforeEach
            void setUp() {
                categoryIds = List.of(UUID.randomUUID(), UUID.randomUUID());
                projections = List.of(
                        createMockProjection(categoryIds.get(0), 5L), createMockProjection(categoryIds.get(1), 3L));
                doReturn(projections).when(repository).countCategorized(categoryIds);
            }

            private RecipeBookmarkCategorizedCountProjection createMockProjection(UUID categoryId, Long count) {
                RecipeBookmarkCategorizedCountProjection projection =
                        mock(RecipeBookmarkCategorizedCountProjection.class);
                doReturn(categoryId).when(projection).getCategoryId();
                doReturn(count).when(projection).getCount();
                return projection;
            }

            @Nested
            @DisplayName("When - 개수 조회를 요청하면")
            class WhenCounting {
                List<RecipeBookmarkCategorizedCount> result;

                @BeforeEach
                void setUp() {
                    result = service.countByCategories(categoryIds);
                }

                @Test
                @DisplayName("Then - 각 카테고리의 개수를 반환한다")
                void thenReturnsCounts() {
                    assertThat(result).hasSize(2);
                    assertThat(result.getFirst().getCategoryId()).isEqualTo(categoryIds.getFirst());
                    assertThat(result.getFirst().getCount()).isEqualTo(5);
                    assertThat(result.get(1).getCategoryId()).isEqualTo(categoryIds.get(1));
                    assertThat(result.get(1).getCount()).isEqualTo(3);
                    verify(repository).countCategorized(categoryIds);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 목록 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 레시피 ID 목록과 사용자 ID가 주어졌을 때")
        class GivenRecipeIds {
            List<UUID> recipeIds;
            UUID userId;

            @BeforeEach
            void setUp() {
                recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeBookmark> bookmarks;
                List<RecipeBookmark> result;

                @BeforeEach
                void setUp() {
                    bookmarks = List.of(mock(RecipeBookmark.class), mock(RecipeBookmark.class));
                    doReturn(bookmarks).when(repository).finds(userId, recipeIds);
                    result = service.gets(recipeIds, userId);
                }

                @Test
                @DisplayName("Then - 북마크 목록을 반환한다")
                void thenReturnsBookmarks() {
                    assertThat(result).hasSize(2);
                    assertThat(result).containsExactlyElementsOf(bookmarks);
                    verify(repository).finds(userId, recipeIds);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 레시피 ID 목록일 때")
        class GivenEmptyIds {
            UUID userId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeBookmark> result;

                @BeforeEach
                void setUp() {
                    doReturn(List.of()).when(repository).finds(userId, List.of());
                    result = service.gets(List.of(), userId);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmpty() {
                    assertThat(result).isEmpty();
                    verify(repository).finds(userId, List.of());
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 삭제 (delete)")
    class Delete {

        @Nested
        @DisplayName("Given - 유효한 ID들이 주어졌을 때")
        class GivenValidIds {
            UUID recipeId;
            UUID userId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    service.delete(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 북마크를 삭제한다")
                void thenDeletes() throws RecipeBookmarkException {
                    verify(repository).delete(userId, recipeId, clock);
                }
            }
        }
    }

    @Nested
    @DisplayName("미분류 개수 조회 (countUncategorized)")
    class CountUncategorized {

        @Nested
        @DisplayName("Given - 사용자 ID가 주어졌을 때")
        class GivenUserId {
            UUID userId;
            RecipeBookmarkUnCategorizedCountProjection projection;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                projection = mock(RecipeBookmarkUnCategorizedCountProjection.class);
                doReturn(projection).when(repository).countUncategorized(userId);
            }

            @Nested
            @DisplayName("When - 개수가 있을 때")
            class WhenCountExists {
                RecipeBookmarkUnCategorizedCount result;

                @BeforeEach
                void setUp() {
                    doReturn(5L).when(projection).getCount();
                    result = service.countUncategorized(userId);
                }

                @Test
                @DisplayName("Then - 개수를 반환한다")
                void thenReturnsCount() {
                    assertThat(result.getCount()).isEqualTo(5);
                    verify(repository).countUncategorized(userId);
                }
            }

            @Nested
            @DisplayName("When - 개수가 없을 때")
            class WhenCountZero {
                RecipeBookmarkUnCategorizedCount result;

                @BeforeEach
                void setUp() {
                    doReturn(0L).when(projection).getCount();
                    result = service.countUncategorized(userId);
                }

                @Test
                @DisplayName("Then - 0을 반환한다")
                void thenReturnsZero() {
                    assertThat(result.getCount()).isEqualTo(0);
                    verify(repository).countUncategorized(userId);
                }
            }
        }
    }

    @Nested
    @DisplayName("일괄 삭제 (deletes)")
    class Deletes {

        @Nested
        @DisplayName("Given - 북마크 ID 목록이 주어졌을 때")
        class GivenBookmarkIds {
            List<UUID> bookmarkIds;

            @BeforeEach
            void setUp() {
                bookmarkIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() {
                    service.deletes(bookmarkIds);
                }

                @Test
                @DisplayName("Then - 일괄 삭제한다")
                void thenDeletesAll() {
                    verify(repository).deletes(bookmarkIds, clock);
                }
            }
        }
    }

    @Nested
    @DisplayName("차단 (block)")
    class Block {

        @Nested
        @DisplayName("Given - 레시피 ID가 주어졌을 때")
        class GivenRecipeId {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 차단을 요청하면")
            class WhenBlocking {

                @BeforeEach
                void setUp() {
                    service.block(recipeId);
                }

                @Test
                @DisplayName("Then - 해당 레시피의 북마크를 차단한다")
                void thenBlocks() {
                    verify(repository).block(recipeId, clock);
                }
            }
        }
    }
}
