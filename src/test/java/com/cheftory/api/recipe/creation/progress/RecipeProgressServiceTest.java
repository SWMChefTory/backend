package com.cheftory.api.recipe.creation.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import org.mockito.ArgumentCaptor;

@DisplayName("RecipeProgressService 테스트")
class RecipeProgressServiceTest {

    private RecipeProgressRepository repository;
    private Clock clock;
    private RecipeProgressService service;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeProgressRepository.class);
        clock = mock(Clock.class);
        doReturn(LocalDateTime.of(2026, 1, 1, 0, 0)).when(clock).now();
        service = new RecipeProgressService(repository, clock);
    }

    @Nested
    @DisplayName("조회 (gets)")
    class Gets {
        @Test
        @DisplayName("recipeId/jobId로 정렬 조회를 위임한다")
        void delegatesByRecipeAndJobId() {
            UUID recipeId = UUID.randomUUID();
            UUID jobId = UUID.randomUUID();
            List<RecipeProgress> expected = List.of(mock(RecipeProgress.class));
            doReturn(expected)
                    .when(repository)
                    .findAllByRecipeIdAndJobId(recipeId, jobId, RecipeProgressSort.CREATE_AT_ASC);

            List<RecipeProgress> result = service.gets(recipeId, jobId);

            assertThat(result).isEqualTo(expected);
            verify(repository).findAllByRecipeIdAndJobId(recipeId, jobId, RecipeProgressSort.CREATE_AT_ASC);
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 목록을 그대로 반환한다")
        void returnsEmpty() {
            UUID recipeId = UUID.randomUUID();
            UUID jobId = UUID.randomUUID();
            doReturn(List.of())
                    .when(repository)
                    .findAllByRecipeIdAndJobId(recipeId, jobId, RecipeProgressSort.CREATE_AT_ASC);

            List<RecipeProgress> result = service.gets(recipeId, jobId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("상태 기록 (start/success/failed)")
    class ProgressWrites {
        UUID recipeId;
        UUID jobId;

        @BeforeEach
        void setUp() {
            recipeId = UUID.randomUUID();
            jobId = UUID.randomUUID();
        }

        @Test
        @DisplayName("start - RUNNING 이벤트를 저장한다")
        void startSavesRunningEvent() {
            service.start(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP, jobId);

            ArgumentCaptor<RecipeProgress> captor = ArgumentCaptor.forClass(RecipeProgress.class);
            verify(repository).save(captor.capture());

            RecipeProgress saved = captor.getValue();
            assertThat(saved.getRecipeId()).isEqualTo(recipeId);
            assertThat(saved.getJobId()).isEqualTo(jobId);
            assertThat(saved.getStep()).isEqualTo(RecipeProgressStep.STEP);
            assertThat(saved.getDetail()).isEqualTo(RecipeProgressDetail.STEP);
            assertThat(saved.getState()).isEqualTo(RecipeProgressState.RUNNING);
        }

        @Test
        @DisplayName("success - SUCCESS 이벤트를 저장한다")
        void successSavesSuccessEvent() {
            service.success(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP, jobId);

            ArgumentCaptor<RecipeProgress> captor = ArgumentCaptor.forClass(RecipeProgress.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getState()).isEqualTo(RecipeProgressState.SUCCESS);
        }

        @Test
        @DisplayName("failed - FAILED 이벤트를 저장한다")
        void failedSavesFailedEvent() {
            service.failed(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP, jobId);

            ArgumentCaptor<RecipeProgress> captor = ArgumentCaptor.forClass(RecipeProgress.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getState()).isEqualTo(RecipeProgressState.FAILED);
        }
    }
}
