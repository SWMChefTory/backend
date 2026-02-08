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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeBookmarkRepository Tests")
@DataJpaTest
@Import({RecipeBookmarkRepositoryImpl.class, ViewedAtCursorCodec.class})
public class RecipeBookmarkRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeBookmarkRepository recipeBookmarkRepository;

    @Autowired
    private com.cheftory.api.recipe.bookmark.repository.RecipeBookmarkJpaRepository recipeBookmarkJpaRepository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.now()).thenReturn(LocalDateTime.now());
    }

    @Nested
    @DisplayName("레시피 북마크 저장")
    class SaveRecipeBookmark {

        @Test
        @DisplayName("레시피 북마크가 저장된다")
        void shouldSaveRecipeBookmark() {
            UUID recipeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            when(clock.now()).thenReturn(LocalDateTime.now());

            RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
            recipeBookmarkRepository.create(bookmark);

            RecipeBookmark saved = recipeBookmarkRepository.get(userId, recipeId);
            assertThat(saved.getRecipeId()).isEqualTo(recipeId);
            assertThat(saved.getUserId()).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("재생성 쿼리")
    class RecreateBookmark {

        @Test
        @DisplayName("삭제된 북마크를 재생성한다")
        void shouldRecreateDeletedBookmark() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
            recipeBookmarkRepository.create(bookmark);
            recipeBookmarkRepository.delete(userId, recipeId, clock);

            boolean result = recipeBookmarkRepository.recreate(userId, recipeId, clock);

            assertThat(result).isTrue();
            RecipeBookmark recreated = recipeBookmarkRepository.get(userId, recipeId);
            assertThat(recreated.getStatus()).isEqualTo(RecipeBookmarkStatus.ACTIVE);
        }

        @Test
        @DisplayName("활성 북마크 재생성 시 false를 반환한다")
        void shouldReturnFalseWhenBookmarkActive() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
            recipeBookmarkRepository.create(bookmark);

            boolean result = recipeBookmarkRepository.recreate(userId, recipeId, clock);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 북마크 재생성 시 예외를 던진다")
        void shouldThrowExceptionWhenBookmarkNotFound() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            assertThatThrownBy(() -> recipeBookmarkRepository.recreate(userId, recipeId, clock))
                    .isInstanceOf(RecipeBookmarkException.class)
                    .extracting("error")
                    .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("북마크 존재 여부 확인")
    class ExistsBookmark {

        @Test
        @DisplayName("북마크가 존재하면 true를 반환한다")
        void shouldReturnTrueWhenBookmarkExists() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
            recipeBookmarkRepository.create(bookmark);

            boolean result = recipeBookmarkRepository.exists(userId, recipeId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("북마크가 존재하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenBookmarkNotExists() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            boolean result = recipeBookmarkRepository.exists(userId, recipeId);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("북마크 카테고리 설정")
    class CategorizeBookmark {

        @Test
        @DisplayName("북마크에 카테고리를 설정한다")
        void shouldSetCategory() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
            recipeBookmarkRepository.create(bookmark);

            recipeBookmarkRepository.categorize(userId, recipeId, categoryId);

            RecipeBookmark result = recipeBookmarkRepository.get(userId, recipeId);
            assertThat(result.getRecipeCategoryId()).isEqualTo(categoryId);
        }

        @Test
        @DisplayName("존재하지 않는 북마크 카테고리 설정 시 예외를 던진다")
        void shouldThrowExceptionWhenBookmarkNotFound() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            assertThatThrownBy(() -> recipeBookmarkRepository.categorize(userId, recipeId, categoryId))
                    .isInstanceOf(RecipeBookmarkException.class)
                    .extracting("error")
                    .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("북마크 카테고리 해제")
    class UnCategorizeBookmark {

        @Test
        @DisplayName("카테고리별 북마크의 카테고리를 해제한다")
        void shouldUnsetCategory() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
            recipeBookmarkRepository.create(bookmark);
            recipeBookmarkRepository.categorize(userId, recipeId, categoryId);

            recipeBookmarkRepository.unCategorize(categoryId);

            RecipeBookmark result = recipeBookmarkRepository.get(userId, recipeId);
            assertThat(result.getRecipeCategoryId()).isNull();
        }
    }

    @Nested
    @DisplayName("북마크 삭제")
    class DeleteBookmark {

        @Test
        @DisplayName("북마크를 삭제한다")
        void shouldDeleteBookmark() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeBookmark bookmark = RecipeBookmark.create(clock, userId, recipeId);
            recipeBookmarkRepository.create(bookmark);

            recipeBookmarkRepository.delete(userId, recipeId, clock);

            RecipeBookmark result =
                    recipeBookmarkJpaRepository.findById(bookmark.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(RecipeBookmarkStatus.DELETED);
        }

        @Test
        @DisplayName("존재하지 않는 북마크 삭제 시 예외를 던진다")
        void shouldThrowExceptionWhenBookmarkNotFound() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            assertThatThrownBy(() -> recipeBookmarkRepository.delete(userId, recipeId, clock))
                    .isInstanceOf(RecipeBookmarkException.class)
                    .extracting("error")
                    .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("북마크 일괄 삭제")
    class DeletesBookmark {

        @Test
        @DisplayName("여러 북마크를 일괄 삭제한다")
        void shouldDeleteMultipleBookmarks() {
            UUID userId = UUID.randomUUID();
            UUID recipeId1 = UUID.randomUUID();
            UUID recipeId2 = UUID.randomUUID();
            UUID recipeId3 = UUID.randomUUID();

            RecipeBookmark bookmark1 = RecipeBookmark.create(clock, userId, recipeId1);
            RecipeBookmark bookmark2 = RecipeBookmark.create(clock, userId, recipeId2);
            RecipeBookmark bookmark3 = RecipeBookmark.create(clock, userId, recipeId3);
            recipeBookmarkRepository.create(bookmark1);
            recipeBookmarkRepository.create(bookmark2);
            recipeBookmarkRepository.create(bookmark3);

            recipeBookmarkRepository.deletes(List.of(bookmark1.getId(), bookmark2.getId(), bookmark3.getId()), clock);

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

    @Nested
    @DisplayName("북마크 차단")
    class BlockBookmark {

        @Test
        @DisplayName("레시피별 북마크를 차단한다")
        void shouldBlockBookmarksByRecipe() {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeBookmark bookmark1 = RecipeBookmark.create(clock, userId1, recipeId);
            RecipeBookmark bookmark2 = RecipeBookmark.create(clock, userId2, recipeId);
            recipeBookmarkRepository.create(bookmark1);
            recipeBookmarkRepository.create(bookmark2);

            recipeBookmarkRepository.block(recipeId, clock);

            List<RecipeBookmark> blocked = recipeBookmarkJpaRepository.findAllByRecipeId(recipeId);
            assertThat(blocked).hasSize(2);
            assertThat(blocked.get(0).getStatus()).isEqualTo(RecipeBookmarkStatus.BLOCKED);
            assertThat(blocked.get(1).getStatus()).isEqualTo(RecipeBookmarkStatus.BLOCKED);
        }
    }

    @Nested
    @DisplayName("북마크 조회")
    class GetBookmark {

        @Test
        @DisplayName("존재하지 않는 북마크 조회 시 예외를 던진다")
        void shouldThrowExceptionWhenBookmarkNotFound() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            assertThatThrownBy(() -> recipeBookmarkRepository.get(userId, recipeId))
                    .isInstanceOf(RecipeBookmarkException.class)
                    .extracting("error")
                    .isEqualTo(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("커서 기반 조회 쿼리")
    class CursorQueries {

        @Test
        @DisplayName("최근 북마크 첫 페이지를 조회한다")
        void shouldFindRecentsFirst() {
            UUID userId = UUID.randomUUID();
            when(clock.now())
                    .thenReturn(
                            LocalDateTime.of(2024, 1, 1, 10, 0),
                            LocalDateTime.of(2024, 1, 1, 11, 0),
                            LocalDateTime.of(2024, 1, 1, 12, 0));

            RecipeBookmark h1 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            RecipeBookmark h2 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            RecipeBookmark h3 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            recipeBookmarkRepository.create(h1);
            recipeBookmarkRepository.create(h2);
            recipeBookmarkRepository.create(h3);

            CursorPage<RecipeBookmark> result = recipeBookmarkRepository.keysetRecentsFirst(userId);

            assertThat(result.items()).hasSize(3);
            assertThat(result.items().getFirst().getViewedAt()).isEqualTo(h3.getViewedAt());
            assertThat(result.items().get(1).getViewedAt()).isEqualTo(h2.getViewedAt());
        }
    }

    @Nested
    @DisplayName("RecipeBookmarkRepositoryImpl")
    class RepositoryImpl {

        @Test
        @DisplayName("최근 커서가 잘못되면 INVALID_CURSOR 예외를 던진다")
        void shouldThrowInvalidCursorForRecents() {
            UUID userId = UUID.randomUUID();
            String cursor = "invalid-cursor";

            CursorException ex =
                    assertThrows(CursorException.class, () -> recipeBookmarkRepository.keysetRecents(userId, cursor));

            assertThat(ex.getError()).isEqualTo(CursorErrorCode.INVALID_CURSOR);
        }

        @Test
        @DisplayName("카테고리 커서가 잘못되면 INVALID_CURSOR 예외를 던진다")
        void shouldThrowInvalidCursorForCategorized() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            String cursor = "invalid-cursor";

            CursorException ex = assertThrows(
                    CursorException.class,
                    () -> recipeBookmarkRepository.keysetCategorized(userId, categoryId, cursor));

            assertThat(ex.getError()).isEqualTo(CursorErrorCode.INVALID_CURSOR);
        }

        @Test
        @DisplayName("삭제 대상 ID가 비어있으면 삭제 쿼리를 수행하지 않는다")
        void shouldSkipDeleteWhenIdsEmpty() {
            recipeBookmarkRepository.deletes(List.of(), clock);
        }

        @Test
        @DisplayName("카테고리별 최근 북마크 첫 페이지를 조회한다")
        void shouldFindCategorizedFirst() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            RecipeBookmark bookmark1 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            RecipeBookmark bookmark2 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            RecipeBookmark bookmark3 = RecipeBookmark.create(clock, userId, UUID.randomUUID());

            recipeBookmarkRepository.create(bookmark1);
            recipeBookmarkRepository.create(bookmark2);
            recipeBookmarkRepository.create(bookmark3);

            recipeBookmarkRepository.categorize(userId, bookmark1.getRecipeId(), categoryId);
            recipeBookmarkRepository.categorize(userId, bookmark2.getRecipeId(), categoryId);
            recipeBookmarkRepository.categorize(userId, bookmark3.getRecipeId(), categoryId);

            CursorPage<RecipeBookmark> result = recipeBookmarkRepository.keysetCategorizedFirst(userId, categoryId);

            assertThat(result.items()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("북마크 개수 조회")
    class CountBookmark {

        @Test
        @DisplayName("카테고리별 북마크 개수를 조회한다")
        void shouldCountByCategories() {
            UUID userId = UUID.randomUUID();
            UUID categoryId1 = UUID.randomUUID();
            UUID categoryId2 = UUID.randomUUID();

            RecipeBookmark bookmark1 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            RecipeBookmark bookmark2 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            RecipeBookmark bookmark3 = RecipeBookmark.create(clock, userId, UUID.randomUUID());

            recipeBookmarkRepository.create(bookmark1);
            recipeBookmarkRepository.create(bookmark2);
            recipeBookmarkRepository.create(bookmark3);

            recipeBookmarkRepository.categorize(userId, bookmark1.getRecipeId(), categoryId1);
            recipeBookmarkRepository.categorize(userId, bookmark2.getRecipeId(), categoryId1);
            recipeBookmarkRepository.categorize(userId, bookmark3.getRecipeId(), categoryId2);

            var result = recipeBookmarkRepository.countCategorized(List.of(categoryId1, categoryId2));

            assertThat(result).hasSize(2);
            assertThat(result).extracting("categoryId").contains(categoryId1, categoryId2);
            assertThat(result).extracting("count").contains(2L, 1L);
        }

        @Test
        @DisplayName("미분류 북마크 개수를 조회한다")
        void shouldCountUncategorized() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            RecipeBookmark bookmark1 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            RecipeBookmark bookmark2 = RecipeBookmark.create(clock, userId, UUID.randomUUID());
            RecipeBookmark bookmark3 = RecipeBookmark.create(clock, userId, UUID.randomUUID());

            recipeBookmarkRepository.create(bookmark1);
            recipeBookmarkRepository.create(bookmark2);
            recipeBookmarkRepository.create(bookmark3);

            recipeBookmarkRepository.categorize(userId, bookmark1.getRecipeId(), categoryId);

            var result = recipeBookmarkRepository.countUncategorized(userId);

            assertThat(result.getCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("북마크 목록 조회")
    class GetsBookmark {

        @Test
        @DisplayName("레시피 ID 목록으로 사용자의 북마크 목록을 조회한다")
        void shouldGetByRecipeIds() {
            UUID userId = UUID.randomUUID();
            UUID recipeId1 = UUID.randomUUID();
            UUID recipeId2 = UUID.randomUUID();
            UUID recipeId3 = UUID.randomUUID();

            RecipeBookmark bookmark1 = RecipeBookmark.create(clock, userId, recipeId1);
            RecipeBookmark bookmark2 = RecipeBookmark.create(clock, userId, recipeId2);
            recipeBookmarkRepository.create(bookmark1);
            recipeBookmarkRepository.create(bookmark2);

            List<RecipeBookmark> result =
                    recipeBookmarkRepository.gets(userId, List.of(recipeId1, recipeId2, recipeId3));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getRecipeId()).isIn(recipeId1, recipeId2);
            assertThat(result.get(1).getRecipeId()).isIn(recipeId1, recipeId2);
        }

        @Test
        @DisplayName("레시피 ID로 모든 사용자의 북마크 목록을 조회한다")
        void shouldGetByRecipeId() {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeBookmark bookmark1 = RecipeBookmark.create(clock, userId1, recipeId);
            RecipeBookmark bookmark2 = RecipeBookmark.create(clock, userId2, recipeId);
            recipeBookmarkRepository.create(bookmark1);
            recipeBookmarkRepository.create(bookmark2);

            List<RecipeBookmark> result = recipeBookmarkRepository.gets(recipeId);

            assertThat(result).hasSize(2);
        }
    }
}
