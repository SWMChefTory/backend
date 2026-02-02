package com.cheftory.api.recipe.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeBookmarkRepository Tests")
public class RecipeBookmarkRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeBookmarkRepository repository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {}

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
            repository.save(bookmark);

            RecipeBookmark saved = repository.findById(bookmark.getId()).orElseThrow();
            assertThat(saved.getRecipeId()).isEqualTo(recipeId);
            assertThat(saved.getUserId()).isEqualTo(userId);
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

            RecipeBookmark h1 = repository.save(RecipeBookmark.create(clock, userId, UUID.randomUUID()));
            RecipeBookmark h2 = repository.save(RecipeBookmark.create(clock, userId, UUID.randomUUID()));
            RecipeBookmark h3 = repository.save(RecipeBookmark.create(clock, userId, UUID.randomUUID()));

            Pageable pageable = PageRequest.of(0, 2);
            List<RecipeBookmark> result = repository.findRecentsFirst(userId, RecipeBookmarkStatus.ACTIVE, pageable);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getViewedAt()).isEqualTo(h3.getViewedAt());
            assertThat(result.get(1).getViewedAt()).isEqualTo(h2.getViewedAt());
        }

        @Test
        @DisplayName("최근 북마크 keyset을 조회한다")
        void shouldFindRecentsKeyset() {
            UUID userId = UUID.randomUUID();
            when(clock.now())
                    .thenReturn(
                            LocalDateTime.of(2024, 1, 1, 10, 0),
                            LocalDateTime.of(2024, 1, 1, 10, 0),
                            LocalDateTime.of(2024, 1, 1, 11, 0),
                            LocalDateTime.of(2024, 1, 1, 11, 0),
                            LocalDateTime.of(2024, 1, 1, 12, 0),
                            LocalDateTime.of(2024, 1, 1, 12, 0));

            RecipeBookmark h1 = repository.save(RecipeBookmark.create(clock, userId, UUID.randomUUID()));
            RecipeBookmark h2 = repository.save(RecipeBookmark.create(clock, userId, UUID.randomUUID()));
            repository.save(RecipeBookmark.create(clock, userId, UUID.randomUUID()));

            Pageable pageable = PageRequest.of(0, 2);
            List<RecipeBookmark> result = repository.findRecentsKeyset(
                    userId, RecipeBookmarkStatus.ACTIVE, h2.getViewedAt(), h2.getId(), pageable);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(h1.getId());
        }
    }
}
