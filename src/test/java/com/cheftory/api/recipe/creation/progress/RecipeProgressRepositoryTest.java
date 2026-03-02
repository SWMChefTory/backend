package com.cheftory.api.recipe.creation.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressState;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.creation.progress.utils.RecipeProgressSort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeProgressRepository 테스트")
class RecipeProgressRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeProgressRepository repository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        doReturn(LocalDateTime.of(2026, 1, 1, 12, 0)).when(clock).now();
    }

    @Nested
    @DisplayName("jobId 기준 조회")
    class FindByRecipeAndJobId {
        UUID recipeId;
        UUID jobId;
        UUID otherJobId;
        UUID otherRecipeId;

        @BeforeEach
        void setUp() {
            recipeId = UUID.randomUUID();
            jobId = UUID.randomUUID();
            otherJobId = UUID.randomUUID();
            otherRecipeId = UUID.randomUUID();

            repository.save(RecipeProgress.create(
                    recipeId,
                    jobId,
                    clock,
                    RecipeProgressStep.READY,
                    RecipeProgressDetail.READY,
                    RecipeProgressState.RUNNING));
            repository.save(RecipeProgress.create(
                    recipeId,
                    jobId,
                    clock,
                    RecipeProgressStep.READY,
                    RecipeProgressDetail.READY,
                    RecipeProgressState.SUCCESS));
            repository.save(RecipeProgress.create(
                    recipeId,
                    otherJobId,
                    clock,
                    RecipeProgressStep.READY,
                    RecipeProgressDetail.READY,
                    RecipeProgressState.RUNNING));
            repository.save(RecipeProgress.create(
                    otherRecipeId,
                    jobId,
                    clock,
                    RecipeProgressStep.READY,
                    RecipeProgressDetail.READY,
                    RecipeProgressState.RUNNING));
        }

        @Test
        @DisplayName("같은 recipeId + jobId의 이벤트만 반환한다")
        void returnsOnlyMatchedJobEvents() {
            List<RecipeProgress> results =
                    repository.findAllByRecipeIdAndJobId(recipeId, jobId, RecipeProgressSort.CREATE_AT_ASC);

            assertThat(results).hasSize(2);
            assertThat(results).extracting(RecipeProgress::getJobId).containsOnly(jobId);
            assertThat(results).extracting(RecipeProgress::getRecipeId).containsOnly(recipeId);
            assertThat(results)
                    .extracting(RecipeProgress::getState)
                    .containsExactly(RecipeProgressState.RUNNING, RecipeProgressState.SUCCESS);
        }

        @Test
        @DisplayName("매칭되는 이벤트가 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenNoMatch() {
            List<RecipeProgress> results = repository.findAllByRecipeIdAndJobId(
                    UUID.randomUUID(), UUID.randomUUID(), RecipeProgressSort.CREATE_AT_ASC);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("생성시각 오름차순으로 반환한다")
        void returnsSortedByCreatedAtAsc() {
            doReturn(
                            LocalDateTime.of(2026, 1, 1, 12, 0, 1),
                            LocalDateTime.of(2026, 1, 1, 12, 0, 2),
                            LocalDateTime.of(2026, 1, 1, 12, 0, 3))
                    .when(clock)
                    .now();

            UUID recipeId = UUID.randomUUID();
            UUID jobId = UUID.randomUUID();

            repository.save(RecipeProgress.create(
                    recipeId,
                    jobId,
                    clock,
                    RecipeProgressStep.READY,
                    RecipeProgressDetail.READY,
                    RecipeProgressState.RUNNING));
            repository.save(RecipeProgress.create(
                    recipeId,
                    jobId,
                    clock,
                    RecipeProgressStep.STEP,
                    RecipeProgressDetail.STEP,
                    RecipeProgressState.RUNNING));
            repository.save(RecipeProgress.create(
                    recipeId,
                    jobId,
                    clock,
                    RecipeProgressStep.FINISHED,
                    RecipeProgressDetail.FINISHED,
                    RecipeProgressState.SUCCESS));

            List<RecipeProgress> results =
                    repository.findAllByRecipeIdAndJobId(recipeId, jobId, RecipeProgressSort.CREATE_AT_ASC);

            assertThat(results).hasSize(3);
            assertThat(results.get(0).getCreatedAt()).isBefore(results.get(1).getCreatedAt());
            assertThat(results.get(1).getCreatedAt()).isBefore(results.get(2).getCreatedAt());
        }
    }
}
