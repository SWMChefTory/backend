package com.cheftory.api.recipe.creation.progress;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressState;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.creation.progress.utils.RecipeProgressSort;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeProgressService 테스트")
public class RecipeProgressServiceTest {

    private RecipeProgressService recipeProgressService;
    private RecipeProgressRepository recipeProgressRepository;
    private Clock clock;

    @BeforeEach
    public void setUp() {
        clock = mock(Clock.class);
        recipeProgressRepository = mock(RecipeProgressRepository.class);
        recipeProgressService = new RecipeProgressService(recipeProgressRepository, clock);
    }

    @Nested
    @DisplayName("레시피 진행 상황 목록 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
        class GivenValidRecipeId {

            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 레시피 진행 상황 목록을 조회하면")
            class WhenGetting {

                private List<RecipeProgress> recipeProgress;

                @BeforeEach
                void setUp() {
                    recipeProgress = List.of(
                            RecipeProgress.create(
                                    recipeId,
                                    clock,
                                    RecipeProgressStep.READY,
                                    RecipeProgressDetail.READY,
                                    RecipeProgressState.SUCCESS),
                            RecipeProgress.create(
                                    recipeId,
                                    clock,
                                    RecipeProgressStep.STEP,
                                    RecipeProgressDetail.CAPTION,
                                    RecipeProgressState.SUCCESS));
                    doReturn(recipeProgress)
                            .when(recipeProgressRepository)
                            .findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
                }

                @DisplayName("Then - 해당 레시피의 진행 상황 목록이 반환된다")
                @Test
                void thenReturnsList() {
                    List<RecipeProgress> results = recipeProgressService.gets(recipeId);
                    assert results.size() == 2;
                    assert results.get(0).getStep() == RecipeProgressStep.READY;
                    assert results.get(1).getDetail() == RecipeProgressDetail.CAPTION;
                }

                @DisplayName("Then - Repository에서 정렬 파라미터와 함께 호출된다")
                @Test
                void thenCallsRepositoryWithSort() {
                    recipeProgressService.gets(recipeId);
                    verify(recipeProgressRepository).findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 진행 상황 정렬 조회 (gets)")
    class GetsWithSort {

        @Nested
        @DisplayName("Given - 시간 순서가 다른 여러 레시피 진행 상황이 있을 때")
        class GivenMultipleProgress {

            private UUID recipeId;
            private List<RecipeProgress> sortedRecipeProgress;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();

                // 시간 순서대로 정렬된 RecipeProgress 목록 생성
                sortedRecipeProgress = List.of(
                        RecipeProgress.create(
                                recipeId,
                                clock,
                                RecipeProgressStep.READY,
                                RecipeProgressDetail.READY,
                                RecipeProgressState.SUCCESS),
                        RecipeProgress.create(
                                recipeId,
                                clock,
                                RecipeProgressStep.CAPTION,
                                RecipeProgressDetail.CAPTION,
                                RecipeProgressState.SUCCESS),
                        RecipeProgress.create(
                                recipeId,
                                clock,
                                RecipeProgressStep.STEP,
                                RecipeProgressDetail.STEP,
                                RecipeProgressState.SUCCESS),
                        RecipeProgress.create(
                                recipeId,
                                clock,
                                RecipeProgressStep.FINISHED,
                                RecipeProgressDetail.FINISHED,
                                RecipeProgressState.SUCCESS));

                doReturn(sortedRecipeProgress)
                        .when(recipeProgressRepository)
                        .findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
            }

            @Nested
            @DisplayName("When - 레시피 진행 상황 목록을 조회하면")
            class WhenGetting {

                @DisplayName("Then - createdAt 오름차순으로 정렬된 결과가 반환된다")
                @Test
                void thenReturnsSortedList() {
                    List<RecipeProgress> results = recipeProgressService.gets(recipeId);

                    assert results.size() == 4;
                    assert results.get(0).getStep() == RecipeProgressStep.READY;
                    assert results.get(1).getStep() == RecipeProgressStep.CAPTION;
                    assert results.get(2).getStep() == RecipeProgressStep.STEP;
                    assert results.get(3).getStep() == RecipeProgressStep.FINISHED;

                    // Repository 메서드가 올바른 정렬 파라미터와 함께 호출되는지 확인
                    verify(recipeProgressRepository).findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 진행 상황 시작 (start)")
    class Start {

        @Nested
        @DisplayName("Given - 유효한 입력 값이 주어졌을 때")
        class GivenValidInputs {

            private UUID recipeId;
            private RecipeProgressStep step;
            private RecipeProgressDetail detail;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                step = RecipeProgressStep.STEP;
                detail = RecipeProgressDetail.STEP;
            }

            @Nested
            @DisplayName("When - 레시피 진행 상황을 시작하면")
            class WhenStarting {

                @BeforeEach
                void setUp() {
                    doReturn(RecipeProgress.create(recipeId, clock, step, detail, RecipeProgressState.RUNNING))
                            .when(recipeProgressRepository)
                            .save(org.mockito.ArgumentMatchers.any(RecipeProgress.class));
                }

                @DisplayName("Then - 레시피 진행 상황이 RUNNING 상태로 생성된다")
                @Test
                void thenStartsProgress() {
                    recipeProgressService.start(recipeId, step, detail);
                    verify(recipeProgressRepository).save(org.mockito.ArgumentMatchers.any(RecipeProgress.class));
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 진행 상황 성공 (success)")
    class Success {

        @Nested
        @DisplayName("Given - 유효한 입력 값이 주어졌을 때")
        class GivenValidInputs {

            private UUID recipeId;
            private RecipeProgressStep step;
            private RecipeProgressDetail detail;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                step = RecipeProgressStep.STEP;
                detail = RecipeProgressDetail.STEP;
            }

            @Nested
            @DisplayName("When - 레시피 진행 상황을 성공으로 표시하면")
            class WhenSuccess {

                @BeforeEach
                void setUp() {
                    doReturn(RecipeProgress.create(recipeId, clock, step, detail, RecipeProgressState.SUCCESS))
                            .when(recipeProgressRepository)
                            .save(org.mockito.ArgumentMatchers.any(RecipeProgress.class));
                }

                @DisplayName("Then - 레시피 진행 상황이 SUCCESS 상태로 생성된다")
                @Test
                void thenSucceedsProgress() {
                    recipeProgressService.success(recipeId, step, detail);
                    verify(recipeProgressRepository).save(org.mockito.ArgumentMatchers.any(RecipeProgress.class));
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 진행 상황 실패 (failed)")
    class Failed {

        @Nested
        @DisplayName("Given - 유효한 입력 값이 주어졌을 때")
        class GivenValidInputs {

            private UUID recipeId;
            private RecipeProgressStep step;
            private RecipeProgressDetail detail;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                step = RecipeProgressStep.STEP;
                detail = RecipeProgressDetail.STEP;
            }

            @Nested
            @DisplayName("When - 레시피 진행 상황을 실패로 표시하면")
            class WhenFailed {

                @BeforeEach
                void setUp() {
                    doReturn(RecipeProgress.create(recipeId, clock, step, detail, RecipeProgressState.FAILED))
                            .when(recipeProgressRepository)
                            .save(org.mockito.ArgumentMatchers.any(RecipeProgress.class));
                }

                @DisplayName("Then - 레시피 진행 상황이 FAILED 상태로 생성된다")
                @Test
                void thenFailsProgress() {
                    recipeProgressService.failed(recipeId, step, detail);
                    verify(recipeProgressRepository).save(org.mockito.ArgumentMatchers.any(RecipeProgress.class));
                }
            }
        }
    }
}
