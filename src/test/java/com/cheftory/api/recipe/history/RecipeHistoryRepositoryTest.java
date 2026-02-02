package com.cheftory.api.recipe.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import com.cheftory.api.recipe.history.entity.RecipeHistoryStatus;
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

@DisplayName("RecipeHistoryRepository Tests")
public class RecipeHistoryRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeHistoryRepository repository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {}

    @Nested
    @DisplayName("레시피 조회 상태 저장")
    class SaveRecipeHistory {

        @Test
        @DisplayName("레시피 조회 상태가 저장된다")
        void shouldSaveRecipeHistory() {
            UUID recipeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            when(clock.now()).thenReturn(LocalDateTime.now());

            RecipeHistory history = RecipeHistory.create(clock, userId, recipeId);
            repository.save(history);

            RecipeHistory saved = repository.findById(history.getId()).orElseThrow();
            assertThat(saved.getRecipeId()).isEqualTo(recipeId);
            assertThat(saved.getUserId()).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("커서 기반 조회 쿼리")
    class CursorQueries {

        @Test
        @DisplayName("최근 기록 첫 페이지를 조회한다")
        void shouldFindRecentsFirst() {
            UUID userId = UUID.randomUUID();
            when(clock.now())
                    .thenReturn(
                            LocalDateTime.of(2024, 1, 1, 10, 0),
                            LocalDateTime.of(2024, 1, 1, 11, 0),
                            LocalDateTime.of(2024, 1, 1, 12, 0));

            RecipeHistory h1 = repository.save(RecipeHistory.create(clock, userId, UUID.randomUUID()));
            RecipeHistory h2 = repository.save(RecipeHistory.create(clock, userId, UUID.randomUUID()));
            RecipeHistory h3 = repository.save(RecipeHistory.create(clock, userId, UUID.randomUUID()));

            Pageable pageable = PageRequest.of(0, 2);
            List<RecipeHistory> result = repository.findRecentsFirst(userId, RecipeHistoryStatus.ACTIVE, pageable);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getViewedAt()).isEqualTo(h3.getViewedAt());
            assertThat(result.get(1).getViewedAt()).isEqualTo(h2.getViewedAt());
        }

        @Test
        @DisplayName("최근 기록 keyset을 조회한다")
        void shouldFindRecentsKeyset() {
            UUID userId = UUID.randomUUID();
            when(clock.now())
                    .thenReturn(
                            LocalDateTime.of(2024, 1, 1, 10, 0),
                            LocalDateTime.of(2024, 1, 1, 11, 0),
                            LocalDateTime.of(2024, 1, 1, 12, 0));

            RecipeHistory h1 = repository.save(RecipeHistory.create(clock, userId, UUID.randomUUID()));
            RecipeHistory h2 = repository.save(RecipeHistory.create(clock, userId, UUID.randomUUID()));
            repository.save(RecipeHistory.create(clock, userId, UUID.randomUUID()));

            Pageable pageable = PageRequest.of(0, 2);
            List<RecipeHistory> result = repository.findRecentsKeyset(
                    userId, RecipeHistoryStatus.ACTIVE, h2.getViewedAt(), h2.getId(), pageable);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(h1.getId());
        }
    }
}
