package com.cheftory.api.recipe.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.ViewedAtCursor;
import com.cheftory.api._common.cursor.ViewedAtCursorCodec;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkStatus;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

@DisplayName("RecipeBookmarkService Tests")
public class RecipeBookmarkServiceTest {

    private RecipeBookmarkRepository repository;
    private RecipeBookmarkService service;
    private Clock clock;
    private ViewedAtCursorCodec viewedAtCursorCodec;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeBookmarkRepository.class);
        clock = mock(Clock.class);
        viewedAtCursorCodec = mock(ViewedAtCursorCodec.class);
        service = new RecipeBookmarkService(repository, viewedAtCursorCodec, clock);
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
            @DisplayName("Then - save가 호출되고 true를 반환해야 한다")
            void thenShouldCallSaveAndReturnTrue() {
                boolean result = service.create(userId, recipeId);

                assertThat(result).isTrue();

                verify(repository)
                        .save(argThat(h -> h.getRecipeId().equals(recipeId)
                                && h.getUserId().equals(userId)));

                verify(repository, never()).findByUserIdAndRecipeId(any(), any());
                verify(repository, never()).existsByUserIdAndRecipeId(any(), any());
                verify(repository, never()).existsByRecipeIdAndUserIdAndStatus(any(), any(), any());
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
            @DisplayName("Then - save에서 무결성 예외 발생 + 기존이 ACTIVE면 false를 반환해야 한다")
            void thenShouldReturnFalseWhenDuplicateAndActive() {
                doThrow(new DataIntegrityViolationException("duplicate"))
                        .when(repository)
                        .save(any(RecipeBookmark.class));

                RecipeBookmark existing = mock(RecipeBookmark.class);
                doReturn(RecipeBookmarkStatus.ACTIVE).when(existing).getStatus();
                doReturn(Optional.of(existing)).when(repository).findByUserIdAndRecipeId(userId, recipeId);

                boolean result = service.create(userId, recipeId);

                assertThat(result).isFalse();

                verify(repository).save(any(RecipeBookmark.class));
                verify(repository).findByUserIdAndRecipeId(userId, recipeId);
                verify(repository, times(1)).save(any(RecipeBookmark.class));
            }

            @Test
            @DisplayName("Then - save에서 무결성 예외 발생 + 기존이 BLOCKED면 false를 반환해야 한다")
            void thenShouldReturnFalseWhenDuplicateAndBlocked() {
                doThrow(new DataIntegrityViolationException("duplicate"))
                        .when(repository)
                        .save(any(RecipeBookmark.class));

                RecipeBookmark existing = mock(RecipeBookmark.class);
                doReturn(RecipeBookmarkStatus.BLOCKED).when(existing).getStatus();
                doReturn(Optional.of(existing)).when(repository).findByUserIdAndRecipeId(userId, recipeId);

                boolean result = service.create(userId, recipeId);

                assertThat(result).isFalse();

                verify(repository).save(any(RecipeBookmark.class));
                verify(repository).findByUserIdAndRecipeId(userId, recipeId);
                verify(repository, times(1)).save(any(RecipeBookmark.class));
            }

            @Test
            @DisplayName("Then - save에서 무결성 예외 발생 + 기존이 DELETED면 ACTIVE로 복구 후 true를 반환해야 한다")
            void thenShouldReactivateWhenDuplicateAndDeleted() {
                RecipeBookmark deleted = mock(RecipeBookmark.class);
                doReturn(RecipeBookmarkStatus.DELETED).when(deleted).getStatus();
                doReturn(Optional.of(deleted)).when(repository).findByUserIdAndRecipeId(userId, recipeId);

                doThrow(new DataIntegrityViolationException("duplicate"))
                        .doReturn(deleted)
                        .when(repository)
                        .save(any(RecipeBookmark.class));

                boolean result = service.create(userId, recipeId);

                assertThat(result).isTrue();

                verify(repository).findByUserIdAndRecipeId(userId, recipeId);
                verify(deleted).active(clock);
                verify(repository, times(2)).save(any(RecipeBookmark.class));
            }

            @Test
            @DisplayName("Then - save에서 무결성 예외 발생 + find 결과 없으면 예외를 다시 던져야 한다")
            void thenShouldRethrowWhenNotFoundExistingRow() {
                doThrow(new DataIntegrityViolationException("constraint"))
                        .when(repository)
                        .save(any(RecipeBookmark.class));

                doReturn(Optional.empty()).when(repository).findByUserIdAndRecipeId(userId, recipeId);

                assertThrows(DataIntegrityViolationException.class, () -> service.create(userId, recipeId));

                verify(repository).save(any(RecipeBookmark.class));
                verify(repository).findByUserIdAndRecipeId(userId, recipeId);
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
            UUID id = UUID.randomUUID();
            LocalDateTime viewedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

            RecipeBookmark bookmark = mock(RecipeBookmark.class);
            doReturn(viewedAt).when(bookmark).getViewedAt();
            doReturn(id).when(bookmark).getId();

            List<RecipeBookmark> rows = java.util.Collections.nCopies(21, bookmark);

            doReturn(rows)
                    .when(repository)
                    .findRecentsFirst(eq(userId), eq(RecipeBookmarkStatus.ACTIVE), any(Pageable.class));

            doReturn("next-cursor").when(viewedAtCursorCodec).encode(any(ViewedAtCursor.class));

            CursorPage<RecipeBookmark> result = service.getRecents(userId, null);

            assertThat(result.items()).hasSize(20);
            assertThat(result.nextCursor()).isEqualTo("next-cursor");

            verify(repository)
                    .findRecentsFirst(eq(userId), eq(RecipeBookmarkStatus.ACTIVE), argThat(p -> p.getPageSize() == 21));
        }

        @Test
        @DisplayName("cursor가 있으면 최근 북마크 keyset을 조회한다")
        void shouldGetRecentsKeysetWithCursor() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            LocalDateTime viewedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            ViewedAtCursor decoded = new ViewedAtCursor(viewedAt, recipeId);

            RecipeBookmark bookmark = mock(RecipeBookmark.class);
            doReturn(viewedAt.minusMinutes(1)).when(bookmark).getViewedAt();
            doReturn(UUID.randomUUID()).when(bookmark).getId();

            doReturn(decoded).when(viewedAtCursorCodec).decode("cursor");
            doReturn(List.of(bookmark))
                    .when(repository)
                    .findRecentsKeyset(
                            eq(userId),
                            eq(RecipeBookmarkStatus.ACTIVE),
                            eq(viewedAt),
                            eq(recipeId),
                            any(Pageable.class));

            CursorPage<RecipeBookmark> result = service.getRecents(userId, "cursor");

            assertThat(result.items()).hasSize(1);
            verify(repository)
                    .findRecentsKeyset(
                            eq(userId),
                            eq(RecipeBookmarkStatus.ACTIVE),
                            eq(viewedAt),
                            eq(recipeId),
                            any(Pageable.class));
        }

        @Test
        @DisplayName("커서 기반 카테고리 조회는 keyset을 사용한다")
        void shouldGetCategorizedKeysetWithCursor() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            LocalDateTime viewedAt = LocalDateTime.of(2024, 1, 2, 10, 0, 0);
            ViewedAtCursor decoded = new ViewedAtCursor(viewedAt, recipeId);

            RecipeBookmark bookmark = mock(RecipeBookmark.class);
            doReturn(viewedAt.minusMinutes(1)).when(bookmark).getViewedAt();
            doReturn(UUID.randomUUID()).when(bookmark).getId();

            doReturn(decoded).when(viewedAtCursorCodec).decode("cursor");
            doReturn(List.of(bookmark))
                    .when(repository)
                    .findCategorizedKeyset(
                            eq(userId),
                            eq(categoryId),
                            eq(RecipeBookmarkStatus.ACTIVE),
                            eq(viewedAt),
                            eq(recipeId),
                            any(Pageable.class));

            CursorPage<RecipeBookmark> result = service.getCategorized(userId, categoryId, "cursor");

            assertThat(result.items()).hasSize(1);
            verify(repository)
                    .findCategorizedKeyset(
                            eq(userId),
                            eq(categoryId),
                            eq(RecipeBookmarkStatus.ACTIVE),
                            eq(viewedAt),
                            eq(recipeId),
                            any(Pageable.class));
        }

        @Test
        @DisplayName("커서 기반 미분류 조회는 keyset을 사용한다")
        void shouldGetUncategorizedKeysetWithCursor() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            LocalDateTime viewedAt = LocalDateTime.of(2024, 1, 3, 10, 0, 0);
            ViewedAtCursor decoded = new ViewedAtCursor(viewedAt, recipeId);

            RecipeBookmark bookmark = mock(RecipeBookmark.class);
            doReturn(viewedAt.minusMinutes(1)).when(bookmark).getViewedAt();
            doReturn(UUID.randomUUID()).when(bookmark).getId();

            doReturn(decoded).when(viewedAtCursorCodec).decode("cursor");
            doReturn(List.of(bookmark))
                    .when(repository)
                    .findUncategorizedKeyset(
                            eq(userId),
                            eq(RecipeBookmarkStatus.ACTIVE),
                            eq(viewedAt),
                            eq(recipeId),
                            any(Pageable.class));

            CursorPage<RecipeBookmark> result = service.getUnCategorized(userId, "cursor");

            assertThat(result.items()).hasSize(1);
            verify(repository)
                    .findUncategorizedKeyset(
                            eq(userId),
                            eq(RecipeBookmarkStatus.ACTIVE),
                            eq(viewedAt),
                            eq(recipeId),
                            any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("레시피 북마크 조회")
    class FindRecipeBookmark {

        @Nested
        @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
        class GivenValidRecipeAndUserId {

            private UUID recipeId;
            private UUID userId;
            private LocalDateTime initialTime;
            private LocalDateTime updateTime;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
                initialTime = LocalDateTime.of(2023, 12, 31, 12, 0, 0);
                updateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
            }

            @Nested
            @DisplayName("When - 레시피 북마크를 조회한다면")
            class WhenFindingRecipeBookmark {

                private RecipeBookmark realRecipeBookmark;

                @BeforeEach
                void beforeEach() {
                    doReturn(initialTime).when(clock).now();
                    realRecipeBookmark = RecipeBookmark.create(clock, userId, recipeId);

                    doReturn(updateTime).when(clock).now();

                    doReturn(Optional.of(realRecipeBookmark))
                            .when(repository)
                            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE);
                    doReturn(realRecipeBookmark).when(repository).save(any(RecipeBookmark.class));
                }

                @Test
                @DisplayName("Then - getWithView 메서드는 viewedAt을 업데이트하고 반환해야 한다")
                public void thenShouldReturnCorrectRecipeBookmark() {
                    RecipeBookmark status = service.getWithView(userId, recipeId);

                    assertThat(status).isNotNull();
                    assertThat(status.getRecipeId()).isEqualTo(recipeId);
                    assertThat(status.getUserId()).isEqualTo(userId);
                    assertThat(status.getViewedAt()).isEqualTo(updateTime);

                    verify(repository).findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE);
                    verify(repository).save(realRecipeBookmark);
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

                private RecipeBookmark realRecipeBookmark;

                @BeforeEach
                void beforeEach() {
                    realRecipeBookmark = RecipeBookmark.create(clock, userId, recipeId);
                    doReturn(Optional.of(realRecipeBookmark))
                            .when(repository)
                            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE);
                    doReturn(realRecipeBookmark).when(repository).save(any(RecipeBookmark.class));
                }

                @Test
                @DisplayName("Then - 카테고리가 올바르게 변경되어야 한다")
                public void thenShouldChangeCategoryCorrectly() {
                    service.updateCategory(userId, recipeId, newCategoryId);

                    assertThat(realRecipeBookmark.getRecipeCategoryId()).isEqualTo(newCategoryId);
                    verify(repository)
                            .save(argThat(h -> h.getRecipeId().equals(recipeId)
                                    && h.getUserId().equals(userId)
                                    && h.getRecipeCategoryId().equals(newCategoryId)));
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID와 사용자 ID가 주어졌을 때")
        class GivenNonExistentRecipeAndUserId {

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
            class WhenChangingNonExistentRecipeBookmarkRecipeCategory {

                @BeforeEach
                void beforeEach() {
                    doReturn(Optional.empty())
                            .when(repository)
                            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - RecipeBookmarkException이 발생해야 한다")
                public void thenShouldThrowViewStatusException() {
                    RecipeBookmarkException exception = assertThrows(
                            RecipeBookmarkException.class,
                            () -> service.updateCategory(userId, recipeId, newCategoryId));

                    assertThat(exception.getErrorMessage())
                            .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
                    verify(repository, never()).save(any());
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
            private LocalDateTime fixedTime;

            @BeforeEach
            void setUp() {
                categoryId = UUID.randomUUID();
                fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                doReturn(fixedTime).when(clock).now();
            }

            @Nested
            @DisplayName("When - 해당 카테고리를 가진 레시피 북마크들이 존재한다면")
            class WhenRecipeBookmarksWithRecipeCategoryExist {

                private List<RecipeBookmark> viewStatusesWithCategory;

                @BeforeEach
                void beforeEach() {
                    RecipeBookmark status1 = RecipeBookmark.create(clock, UUID.randomUUID(), UUID.randomUUID());
                    status1.updateRecipeCategoryId(categoryId);

                    RecipeBookmark status2 = RecipeBookmark.create(clock, UUID.randomUUID(), UUID.randomUUID());
                    status2.updateRecipeCategoryId(categoryId);

                    viewStatusesWithCategory = List.of(status1, status2);

                    doReturn(viewStatusesWithCategory)
                            .when(repository)
                            .findByRecipeCategoryIdAndStatus(categoryId, RecipeBookmarkStatus.ACTIVE);
                    doReturn(viewStatusesWithCategory).when(repository).saveAll(any());
                }

                @Test
                @DisplayName("Then - 해당 카테고리를 가진 모든 레시피 북마크의 카테고리가 비워져야 한다")
                public void thenShouldEmptyAllRecipeBookmarkCategories() {
                    service.unCategorize(categoryId);

                    assertThat(viewStatusesWithCategory).allMatch(status -> status.getRecipeCategoryId() == null);

                    verify(repository).saveAll(argThat(statuses -> {
                        List<RecipeBookmark> statusList = (List<RecipeBookmark>) statuses;
                        return statusList.size() == 2
                                && statusList.stream().allMatch(s -> s.getRecipeCategoryId() == null);
                    }));
                }
            }

            @Nested
            @DisplayName("When - 해당 카테고리를 가진 레시피 북마크가 없다면")
            class WhenNoRecipeBookmarksWithRecipeCategoryExist {

                @BeforeEach
                void beforeEach() {
                    doReturn(List.of())
                            .when(repository)
                            .findByRecipeCategoryIdAndStatus(categoryId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - saveAll이 빈 리스트로 호출되어야 한다")
                public void thenShouldCallSaveAllWithEmptyList() {
                    service.unCategorize(categoryId);

                    verify(repository).findByRecipeCategoryIdAndStatus(categoryId, RecipeBookmarkStatus.ACTIVE);
                    verify(repository).saveAll(List.of());
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
                            createMockProjection(categoryIds.get(0), 5L), createMockProjection(categoryIds.get(1), 3L));
                    doReturn(projections)
                            .when(repository)
                            .countByCategoryIdsAndStatus(categoryIds, RecipeBookmarkStatus.ACTIVE);
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

                    verify(repository).countByCategoryIdsAndStatus(categoryIds, RecipeBookmarkStatus.ACTIVE);
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
                    viewStatuses = List.of(
                            RecipeBookmark.create(clock, userId, recipeIds.get(0)),
                            RecipeBookmark.create(clock, userId, recipeIds.get(1)));

                    doReturn(viewStatuses)
                            .when(repository)
                            .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - 사용자가 조회한 레시피 상태 목록을 반환해야 한다")
                void thenShouldReturnUserRecipeBookmarks() {
                    List<RecipeBookmark> result = service.getByRecipes(recipeIds, userId);

                    assertThat(result).hasSize(2);
                    assertThat(result).containsExactlyElementsOf(viewStatuses);
                    verify(repository)
                            .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeBookmarkStatus.ACTIVE);
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
                    doReturn(List.of())
                            .when(repository)
                            .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환해야 한다")
                void thenShouldReturnEmptyList() {
                    List<RecipeBookmark> result = service.getByRecipes(recipeIds, userId);

                    assertThat(result).isEmpty();
                    verify(repository)
                            .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeBookmarkStatus.ACTIVE);
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
                    doReturn(List.of())
                            .when(repository)
                            .findByRecipeIdInAndUserIdAndStatus(List.of(), userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환해야 한다")
                void thenShouldReturnEmptyList() {
                    List<RecipeBookmark> result = service.getByRecipes(List.of(), userId);

                    assertThat(result).isEmpty();
                    verify(repository)
                            .findByRecipeIdInAndUserIdAndStatus(List.of(), userId, RecipeBookmarkStatus.ACTIVE);
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

                @BeforeEach
                void beforeEach() {
                    viewStatuses = List.of(
                            RecipeBookmark.create(clock, userId, recipeIds.get(0)),
                            RecipeBookmark.create(clock, userId, recipeIds.get(2)));

                    doReturn(viewStatuses)
                            .when(repository)
                            .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - 조회한 레시피 상태만 반환해야 한다")
                void thenShouldReturnOnlyViewedRecipeStatuses() {
                    List<RecipeBookmark> result = service.getByRecipes(recipeIds, userId);

                    assertThat(result).hasSize(2);
                    assertThat(result.get(0).getRecipeId()).isEqualTo(recipeIds.get(0));
                    assertThat(result.get(1).getRecipeId()).isEqualTo(recipeIds.get(2));
                    verify(repository)
                            .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeBookmarkStatus.ACTIVE);
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

                private RecipeBookmark realRecipeBookmark;

                @BeforeEach
                void beforeEach() {
                    realRecipeBookmark = RecipeBookmark.create(clock, userId, recipeId);
                    doReturn(Optional.of(realRecipeBookmark))
                            .when(repository)
                            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - 레시피 북마크가 삭제되어야 한다")
                public void thenShouldDeleteRecipeBookmark() {
                    service.delete(userId, recipeId);

                    verify(repository).findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE);
                    verify(repository).save(realRecipeBookmark);
                    assertThat(realRecipeBookmark.getStatus() == RecipeBookmarkStatus.DELETED)
                            .isTrue();
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID와 사용자 ID가 주어졌을 떄")
        class GivenNonExistentRecipeAndUserId {

            private UUID recipeId;
            private UUID userId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 레시피 북마크를 삭제한다면")
            class WhenDeletingNonExistentRecipeBookmark {

                @BeforeEach
                void beforeEach() {
                    doReturn(Optional.empty())
                            .when(repository)
                            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - RecipeBookmarkException이 발생해야 한다")
                public void thenShouldThrowViewStatusException() {
                    RecipeBookmarkException exception =
                            assertThrows(RecipeBookmarkException.class, () -> service.delete(userId, recipeId));

                    assertThat(exception.getErrorMessage())
                            .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
                    verify(repository, never()).save(any());
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
                    doReturn(projection).when(repository).countByUserIdAndStatus(userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - 올바른 미분류 레시피 개수를 반환해야 한다")
                void thenShouldReturnCorrectUncategorizedCount() {
                    RecipeBookmarkUnCategorizedCount result = service.countUncategorized(userId);

                    assertThat(result.getCount()).isEqualTo(5);
                    verify(repository).countByUserIdAndStatus(userId, RecipeBookmarkStatus.ACTIVE);
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
                    doReturn(projection).when(repository).countByUserIdAndStatus(userId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - 0을 반환해야 한다")
                void thenShouldReturnZero() {
                    RecipeBookmarkUnCategorizedCount result = service.countUncategorized(userId);

                    assertThat(result.getCount()).isEqualTo(0);
                    verify(repository).countByUserIdAndStatus(userId, RecipeBookmarkStatus.ACTIVE);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피별 북마크 삭제")
    class DeleteByRecipe {

        @Nested
        @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
        class GivenValidRecipeId {

            private UUID recipeId;
            private LocalDateTime fixedTime;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                doReturn(fixedTime).when(clock).now();
            }

            @Nested
            @DisplayName("When - 해당 레시피의 모든 북마크를 삭제한다면")
            class WhenDeletingAllBookmarksByRecipe {

                private List<RecipeBookmark> bookmarks;

                @BeforeEach
                void beforeEach() {
                    RecipeBookmark bookmark1 = RecipeBookmark.create(clock, UUID.randomUUID(), recipeId);
                    RecipeBookmark bookmark2 = RecipeBookmark.create(clock, UUID.randomUUID(), recipeId);
                    RecipeBookmark bookmark3 = RecipeBookmark.create(clock, UUID.randomUUID(), recipeId);
                    bookmarks = List.of(bookmark1, bookmark2, bookmark3);

                    doReturn(bookmarks)
                            .when(repository)
                            .findAllByRecipeIdAndStatus(recipeId, RecipeBookmarkStatus.ACTIVE);

                    doReturn(bookmarks).when(repository).saveAll(anyList());
                }

                @Test
                @DisplayName("Then - 모든 북마크가 삭제 상태로 변경되어야 한다")
                void thenShouldDeleteAllBookmarks() {
                    List<RecipeBookmark> result = service.deleteByRecipe(recipeId);

                    assertThat(result).hasSize(3);
                    assertThat(bookmarks).allMatch(bookmark -> bookmark.getStatus() == RecipeBookmarkStatus.DELETED);

                    verify(repository).findAllByRecipeIdAndStatus(recipeId, RecipeBookmarkStatus.ACTIVE);
                    verify(repository).saveAll(argThat(savedBookmarks -> {
                        List<RecipeBookmark> list = (List<RecipeBookmark>) savedBookmarks;
                        return list.size() == 3
                                && list.stream().allMatch(h -> h.getStatus() == RecipeBookmarkStatus.DELETED);
                    }));
                }
            }
        }

        @Nested
        @DisplayName("Given - 북마크가 없는 레시피 ID가 주어졌을 때")
        class GivenRecipeIdWithNoBookmarks {

            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 해당 레시피의 모든 북마크를 삭제한다면")
            class WhenDeletingAllBookmarksByRecipe {

                @BeforeEach
                void beforeEach() {
                    doReturn(List.of())
                            .when(repository)
                            .findAllByRecipeIdAndStatus(recipeId, RecipeBookmarkStatus.ACTIVE);
                }

                @Test
                @DisplayName("Then - 빈 리스트로 saveAll이 호출되어야 한다")
                void thenShouldSaveEmptyList() {
                    List<RecipeBookmark> result = service.deleteByRecipe(recipeId);

                    assertThat(result).isEmpty();
                    verify(repository).findAllByRecipeIdAndStatus(recipeId, RecipeBookmarkStatus.ACTIVE);
                    verify(repository).saveAll(List.of());
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
            private LocalDateTime fixedTime;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                doReturn(fixedTime).when(clock).now();
            }

            @Nested
            @DisplayName("When - 해당 레시피의 모든 북마크를 차단한다면")
            class WhenBlockingAllBookmarksByRecipe {

                private List<RecipeBookmark> bookmarks;

                @BeforeEach
                void beforeEach() {
                    RecipeBookmark bookmark1 = RecipeBookmark.create(clock, UUID.randomUUID(), recipeId);
                    RecipeBookmark bookmark2 = RecipeBookmark.create(clock, UUID.randomUUID(), recipeId);
                    bookmarks = List.of(bookmark1, bookmark2);

                    doReturn(bookmarks).when(repository).findAllByRecipeId(recipeId);
                    doReturn(bookmarks).when(repository).saveAll(anyList());
                }

                @Test
                @DisplayName("Then - 모든 북마크가 BLOCKED 상태로 변경되어야 한다")
                void thenShouldBlockAllBookmarks() {
                    service.blockByRecipe(recipeId);

                    assertThat(bookmarks).allMatch(h -> h.getStatus() == RecipeBookmarkStatus.BLOCKED);
                    verify(repository).findAllByRecipeId(recipeId);
                    verify(repository).saveAll(argThat(saved -> {
                        List<RecipeBookmark> list = (List<RecipeBookmark>) saved;
                        return list.size() == 2
                                && list.stream().allMatch(h -> h.getStatus() == RecipeBookmarkStatus.BLOCKED);
                    }));
                }
            }
        }

        @Nested
        @DisplayName("Given - 북마크가 없는 레시피 ID가 주어졌을 때")
        class GivenRecipeIdWithNoBookmarks {

            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                doReturn(List.of()).when(repository).findAllByRecipeId(recipeId);
            }

            @Nested
            @DisplayName("When - 해당 레시피의 모든 북마크를 차단한다면")
            class WhenBlockingAllHistoriesByRecipe {

                @Test
                @DisplayName("Then - 빈 리스트로 saveAll이 호출되어야 한다")
                void thenShouldSaveEmptyList() {
                    service.blockByRecipe(recipeId);

                    verify(repository).findAllByRecipeId(recipeId);
                    verify(repository).saveAll(List.of());
                }
            }
        }
    }
}
