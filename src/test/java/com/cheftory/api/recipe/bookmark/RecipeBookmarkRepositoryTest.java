package com.cheftory.api.recipe.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorErrorCode;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.ViewedAtCursorCodec;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkStatus;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.bookmark.repository.RecipeBookmarkJpaRepository;
import com.cheftory.api.recipe.bookmark.repository.RecipeBookmarkRepository;
import com.cheftory.api.recipe.bookmark.repository.RecipeBookmarkRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeBookmarkRepository 테스트")
@Import({RecipeBookmarkRepositoryImpl.class, ViewedAtCursorCodec.class})
public class RecipeBookmarkRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeBookmarkRepository recipeBookmarkRepository;

    @Autowired
    private RecipeBookmarkJpaRepository recipeBookmarkJpaRepository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.now()).thenReturn(LocalDateTime.now());
    }

    @Nested
    @DisplayName("북마크 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 북마크가 주어졌을 때")
        class GivenValidBookmark {
            UUID recipeId;
            UUID userId;
            RecipeBookmark bookmark;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();
                when(clock.now()).thenReturn(LocalDateTime.now());
                bookmark = RecipeBookmark.create(clock, userId, recipeId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    recipeBookmarkRepository.create(bookmark);
                }

                @Test
                @DisplayName("Then - 북마크가 저장된다")
                void thenSaved() throws RecipeBookmarkException {
                    RecipeBookmark saved = recipeBookmarkRepository.find(userId, recipeId);
                    assertThat(saved.getRecipeId()).isEqualTo(recipeId);
                    assertThat(saved.getUserId()).isEqualTo(userId);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 재생성 (recreate)")
    class Recreate {

        @Nested
        @DisplayName("Given - 삭제된 북마크가 있을 때")
        class GivenDeletedBookmark {
            UUID userId;
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
                recipeBookmarkRepository.create(bookmark);
                recipeBookmarkRepository.delete(userId, recipeId, clock);
            }

            @Nested
            @DisplayName("When - 재생성을 요청하면")
            class WhenRecreating {
                boolean result;

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    result = recipeBookmarkRepository.recreate(userId, recipeId, clock);
                }

                @Test
                @DisplayName("Then - true를 반환하고 상태가 ACTIVE로 변경된다")
                void thenRecreated() throws RecipeBookmarkException {
                    assertThat(result).isTrue();
                    RecipeBookmark recreated = recipeBookmarkRepository.find(userId, recipeId);
                    assertThat(recreated.getStatus()).isEqualTo(RecipeBookmarkStatus.ACTIVE);
                }
            }
        }

        @Nested
        @DisplayName("Given - 활성 상태의 북마크가 있을 때")
        class GivenActiveBookmark {
            UUID userId;
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
                recipeBookmarkRepository.create(bookmark);
            }

            @Nested
            @DisplayName("When - 재생성을 요청하면")
            class WhenRecreating {
                boolean result;

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    result = recipeBookmarkRepository.recreate(userId, recipeId, clock);
                }

                @Test
                @DisplayName("Then - false를 반환한다")
                void thenReturnsFalse() {
                    assertThat(result).isFalse();
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 북마크일 때")
        class GivenNonExistingBookmark {
            UUID userId;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 재생성을 요청하면")
            class WhenRecreating {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeBookmarkRepository.recreate(userId, recipeId, clock))
                            .isInstanceOf(RecipeBookmarkException.class)
                            .extracting("error")
                            .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 존재 여부 확인 (exists)")
    class Exists {

        @Nested
        @DisplayName("Given - 존재하는 북마크일 때")
        class GivenExistingBookmark {
            UUID userId;
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
                recipeBookmarkRepository.create(bookmark);
            }

            @Nested
            @DisplayName("When - 확인을 요청하면")
            class WhenChecking {
                boolean result;

                @BeforeEach
                void setUp() {
                    result = recipeBookmarkRepository.exists(userId, recipeId);
                }

                @Test
                @DisplayName("Then - true를 반환한다")
                void thenReturnsTrue() {
                    assertThat(result).isTrue();
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 북마크일 때")
        class GivenNonExistingBookmark {
            UUID userId;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 확인을 요청하면")
            class WhenChecking {
                boolean result;

                @BeforeEach
                void setUp() {
                    result = recipeBookmarkRepository.exists(userId, recipeId);
                }

                @Test
                @DisplayName("Then - false를 반환한다")
                void thenReturnsFalse() {
                    assertThat(result).isFalse();
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 카테고리 설정 (categorize)")
    class Categorize {

        @Nested
        @DisplayName("Given - 존재하는 북마크일 때")
        class GivenExistingBookmark {
            UUID userId;
            UUID recipeId;
            UUID categoryId;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
                recipeBookmarkRepository.create(bookmark);
            }

            @Nested
            @DisplayName("When - 카테고리 설정을 요청하면")
            class WhenCategorizing {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    recipeBookmarkRepository.categorize(userId, recipeId, categoryId);
                }

                @Test
                @DisplayName("Then - 카테고리가 설정된다")
                void thenCategorized() throws RecipeBookmarkException {
                    RecipeBookmark result = recipeBookmarkRepository.find(userId, recipeId);
                    assertThat(result.getRecipeCategoryId()).isEqualTo(categoryId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 북마크일 때")
        class GivenNonExistingBookmark {
            UUID userId;
            UUID recipeId;
            UUID categoryId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 카테고리 설정을 요청하면")
            class WhenCategorizing {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeBookmarkRepository.categorize(userId, recipeId, categoryId))
                            .isInstanceOf(RecipeBookmarkException.class)
                            .extracting("error")
                            .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 카테고리 해제 (unCategorize)")
    class UnCategorize {

        @Nested
        @DisplayName("Given - 카테고리가 설정된 북마크가 있을 때")
        class GivenCategorizedBookmark {
            UUID userId;
            UUID recipeId;
            UUID categoryId;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
                recipeBookmarkRepository.create(bookmark);
                recipeBookmarkRepository.categorize(userId, recipeId, categoryId);
            }

            @Nested
            @DisplayName("When - 해제를 요청하면")
            class WhenUnCategorizing {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    recipeBookmarkRepository.unCategorize(categoryId);
                }

                @Test
                @DisplayName("Then - 카테고리가 해제된다")
                void thenUnCategorized() throws RecipeBookmarkException {
                    RecipeBookmark result = recipeBookmarkRepository.find(userId, recipeId);
                    assertThat(result.getRecipeCategoryId()).isNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 삭제 (delete)")
    class Delete {

        @Nested
        @DisplayName("Given - 존재하는 북마크일 때")
        class GivenExistingBookmark {
            UUID userId;
            UUID recipeId;
            RecipeBookmark bookmark;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                bookmark = RecipeBookmark.create(clock, userId, recipeId);
                recipeBookmarkRepository.create(bookmark);
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    recipeBookmarkRepository.delete(userId, recipeId, clock);
                }

                @Test
                @DisplayName("Then - 상태가 DELETED로 변경된다")
                void thenDeleted() {
                    RecipeBookmark result = recipeBookmarkJpaRepository
                            .findById(bookmark.getId())
                            .orElseThrow();
                    assertThat(result.getStatus()).isEqualTo(RecipeBookmarkStatus.DELETED);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 북마크일 때")
        class GivenNonExistingBookmark {
            UUID userId;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeBookmarkRepository.delete(userId, recipeId, clock))
                            .isInstanceOf(RecipeBookmarkException.class)
                            .extracting("error")
                            .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 일괄 삭제 (deletes)")
    class Deletes {

        @Nested
        @DisplayName("Given - 여러 북마크가 있을 때")
        class GivenMultipleBookmarks {
            UUID userId;
            RecipeBookmark bookmark1;
            RecipeBookmark bookmark2;
            RecipeBookmark bookmark3;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                bookmark1 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                bookmark2 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                bookmark3 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                recipeBookmarkRepository.create(bookmark1);
                recipeBookmarkRepository.create(bookmark2);
                recipeBookmarkRepository.create(bookmark3);
            }

            @Nested
            @DisplayName("When - 일괄 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() {
                    recipeBookmarkRepository.deletes(
                            List.of(bookmark1.getId(), bookmark2.getId(), bookmark3.getId()), clock);
                }

                @Test
                @DisplayName("Then - 모든 북마크가 삭제된다")
                void thenAllDeleted() {
                    assertThat(recipeBookmarkJpaRepository
                                    .findById(bookmark1.getId())
                                    .orElseThrow()
                                    .getStatus())
                            .isEqualTo(RecipeBookmarkStatus.DELETED);
                    assertThat(recipeBookmarkJpaRepository
                                    .findById(bookmark2.getId())
                                    .orElseThrow()
                                    .getStatus())
                            .isEqualTo(RecipeBookmarkStatus.DELETED);
                    assertThat(recipeBookmarkJpaRepository
                                    .findById(bookmark3.getId())
                                    .orElseThrow()
                                    .getStatus())
                            .isEqualTo(RecipeBookmarkStatus.DELETED);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 차단 (block)")
    class Block {

        @Nested
        @DisplayName("Given - 특정 레시피의 북마크들이 있을 때")
        class GivenBookmarksByRecipe {
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                recipeId = UUID.randomUUID();
                RecipeBookmark bookmark1 = RecipeBookmark.create(clock, UUID.randomUUID(), recipeId);
                RecipeBookmark bookmark2 = RecipeBookmark.create(clock, UUID.randomUUID(), recipeId);
                recipeBookmarkRepository.create(bookmark1);
                recipeBookmarkRepository.create(bookmark2);
            }

            @Nested
            @DisplayName("When - 차단을 요청하면")
            class WhenBlocking {

                @BeforeEach
                void setUp() {
                    recipeBookmarkRepository.block(recipeId, clock);
                }

                @Test
                @DisplayName("Then - 해당 레시피의 모든 북마크가 차단된다")
                void thenBlocked() {
                    List<RecipeBookmark> blocked = recipeBookmarkJpaRepository.findAllByRecipeId(recipeId);
                    assertThat(blocked).hasSize(2);
                    assertThat(blocked.get(0).getStatus()).isEqualTo(RecipeBookmarkStatus.BLOCKED);
                    assertThat(blocked.get(1).getStatus()).isEqualTo(RecipeBookmarkStatus.BLOCKED);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 조회 (find)")
    class Find {

        @Nested
        @DisplayName("Given - 존재하지 않는 북마크일 때")
        class GivenNonExistingBookmark {
            UUID userId;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFinding {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeBookmarkRepository.find(userId, recipeId))
                            .isInstanceOf(RecipeBookmarkException.class)
                            .extracting("error")
                            .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("커서 기반 조회 (keyset)")
    class Keyset {

        @Nested
        @DisplayName("Given - 최근 북마크들이 있을 때")
        class GivenRecentBookmarks {
            UUID userId;
            RecipeBookmark h1;
            RecipeBookmark h2;
            RecipeBookmark h3;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                when(clock.now())
                        .thenReturn(
                                LocalDateTime.of(2024, 1, 1, 10, 0),
                                LocalDateTime.of(2024, 1, 1, 11, 0),
                                LocalDateTime.of(2024, 1, 1, 12, 0));

                h1 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                h2 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                h3 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                recipeBookmarkRepository.create(h1);
                recipeBookmarkRepository.create(h2);
                recipeBookmarkRepository.create(h3);
            }

            @Nested
            @DisplayName("When - 첫 페이지 조회를 요청하면")
            class WhenFindingFirstPage {
                CursorPage<RecipeBookmark> result;

                @BeforeEach
                void setUp() throws RecipeBookmarkException, CursorException {
                    result = recipeBookmarkRepository.keysetRecentsFirst(userId);
                }

                @Test
                @DisplayName("Then - 최근 순으로 정렬된 목록을 반환한다")
                void thenReturnsSorted() {
                    assertThat(result.items()).hasSize(3);
                    assertThat(result.items().getFirst().getViewedAt()).isEqualTo(h3.getViewedAt());
                    assertThat(result.items().get(1).getViewedAt()).isEqualTo(h2.getViewedAt());
                }
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 커서일 때")
        class GivenInvalidCursor {
            UUID userId;
            UUID categoryId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
            }

            @Test
            @DisplayName("Then - 최근 조회 시 INVALID_CURSOR 예외를 던진다")
            void thenThrowsExceptionForRecents() {
                CursorException ex = assertThrows(
                        CursorException.class, () -> recipeBookmarkRepository.keysetRecents(userId, "invalid-cursor"));
                assertThat(ex.getError()).isEqualTo(CursorErrorCode.INVALID_CURSOR);
            }

            @Test
            @DisplayName("Then - 카테고리 조회 시 INVALID_CURSOR 예외를 던진다")
            void thenThrowsExceptionForCategorized() {
                CursorException ex = assertThrows(
                        CursorException.class,
                        () -> recipeBookmarkRepository.keysetCategorized(userId, categoryId, "invalid-cursor"));
                assertThat(ex.getError()).isEqualTo(CursorErrorCode.INVALID_CURSOR);
            }
        }
    }

    @Nested
    @DisplayName("북마크 개수 조회 (count)")
    class Count {

        @Nested
        @DisplayName("Given - 카테고리별 북마크들이 있을 때")
        class GivenCategorizedBookmarks {
            UUID userId;
            UUID categoryId1;
            UUID categoryId2;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                categoryId1 = UUID.randomUUID();
                categoryId2 = UUID.randomUUID();

                RecipeBookmark b1 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                RecipeBookmark b2 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                RecipeBookmark b3 = RecipeBookmark.create(clock, userId, UUID.randomUUID());

                recipeBookmarkRepository.create(b1);
                recipeBookmarkRepository.create(b2);
                recipeBookmarkRepository.create(b3);

                recipeBookmarkRepository.categorize(userId, b1.getRecipeId(), categoryId1);
                recipeBookmarkRepository.categorize(userId, b2.getRecipeId(), categoryId1);
                recipeBookmarkRepository.categorize(userId, b3.getRecipeId(), categoryId2);
            }

            @Nested
            @DisplayName("When - 카테고리별 개수 조회를 요청하면")
            class WhenCountingCategorized {
                java.util.List<com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection> result;

                @BeforeEach
                void setUp() {
                    result = recipeBookmarkRepository.countCategorized(List.of(categoryId1, categoryId2));
                }

                @Test
                @DisplayName("Then - 각 카테고리의 북마크 개수를 반환한다")
                void thenReturnsCounts() {
                    assertThat(result).hasSize(2);
                    assertThat(result).extracting("categoryId").contains(categoryId1, categoryId2);
                    assertThat(result).extracting("count").contains(2L, 1L);
                }
            }
        }

        @Nested
        @DisplayName("Given - 미분류 북마크들이 있을 때")
        class GivenUncategorizedBookmarks {
            UUID userId;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                UUID categoryId = UUID.randomUUID();

                RecipeBookmark b1 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                RecipeBookmark b2 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
                RecipeBookmark b3 = RecipeBookmark.create(clock, userId, UUID.randomUUID());

                recipeBookmarkRepository.create(b1);
                recipeBookmarkRepository.create(b2);
                recipeBookmarkRepository.create(b3);

                recipeBookmarkRepository.categorize(userId, b1.getRecipeId(), categoryId);
            }

            @Nested
            @DisplayName("When - 미분류 개수 조회를 요청하면")
            class WhenCountingUncategorized {
                com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection result;

                @BeforeEach
                void setUp() {
                    result = recipeBookmarkRepository.countUncategorized(userId);
                }

                @Test
                @DisplayName("Then - 미분류 북마크 개수를 반환한다")
                void thenReturnsCount() {
                    assertThat(result.getCount()).isEqualTo(2);
                }
            }
        }
    }

    @Nested
    @DisplayName("북마크 목록 조회 (finds)")
    class Finds {

        @Nested
        @DisplayName("Given - 사용자별 북마크들이 있을 때")
        class GivenUserBookmarks {
            UUID userId;
            UUID recipeId1;
            UUID recipeId2;
            UUID recipeId3;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId1 = UUID.randomUUID();
                recipeId2 = UUID.randomUUID();
                recipeId3 = UUID.randomUUID();

                RecipeBookmark b1 = RecipeBookmark.create(clock, userId, recipeId1);
                RecipeBookmark b2 = RecipeBookmark.create(clock, userId, recipeId2);
                recipeBookmarkRepository.create(b1);
                recipeBookmarkRepository.create(b2);
            }

            @Nested
            @DisplayName("When - 레시피 ID 목록으로 조회를 요청하면")
            class WhenFindingByRecipeIds {
                List<RecipeBookmark> result;

                @BeforeEach
                void setUp() {
                    result = recipeBookmarkRepository.finds(userId, List.of(recipeId1, recipeId2, recipeId3));
                }

                @Test
                @DisplayName("Then - 해당 사용자의 북마크 목록을 반환한다")
                void thenReturnsBookmarks() {
                    assertThat(result).hasSize(2);
                    assertThat(result.get(0).getRecipeId()).isIn(recipeId1, recipeId2);
                    assertThat(result.get(1).getRecipeId()).isIn(recipeId1, recipeId2);
                }
            }
        }

        @Nested
        @DisplayName("Given - 레시피별 북마크들이 있을 때")
        class GivenRecipeBookmarks {
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeBookmarkException {
                recipeId = UUID.randomUUID();
                UUID userId1 = UUID.randomUUID();
                UUID userId2 = UUID.randomUUID();

                RecipeBookmark b1 = RecipeBookmark.create(clock, userId1, recipeId);
                RecipeBookmark b2 = RecipeBookmark.create(clock, userId2, recipeId);
                recipeBookmarkRepository.create(b1);
                recipeBookmarkRepository.create(b2);
            }

            @Nested
            @DisplayName("When - 레시피 ID로 조회를 요청하면")
            class WhenFindingByRecipeId {
                List<RecipeBookmark> result;

                @BeforeEach
                void setUp() {
                    result = recipeBookmarkRepository.finds(recipeId);
                }

                @Test
                @DisplayName("Then - 해당 레시피의 모든 북마크를 반환한다")
                void thenReturnsBookmarks() {
                    assertThat(result).hasSize(2);
                }
            }
        }
    }
}
