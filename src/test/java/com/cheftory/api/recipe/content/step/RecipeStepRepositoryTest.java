package com.cheftory.api.recipe.content.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.step.entity.RecipeStepSort;
import com.cheftory.api.recipe.content.step.repository.RecipeStepRepository;
import com.cheftory.api.recipe.content.step.repository.RecipeStepRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({RecipeStepRepositoryImpl.class})
@DisplayName("RecipeStepRepository 테스트")
class RecipeStepRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeStepRepository recipeStepRepository;

    private Clock clock;
    private final LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("레시피 단계 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 여러 레시피 단계가 주어졌을 때")
        class GivenMultipleSteps {
            UUID recipeId;
            RecipeStep step1;
            RecipeStep step2;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                step1 = RecipeStep.create(1, "Step 1", List.of(), 0.0, recipeId, clock);
                step2 = RecipeStep.create(2, "Step 2", List.of(), 30.0, recipeId, clock);
            }

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenSaving {

                @BeforeEach
                void setUp() {
                    recipeStepRepository.create(List.of(step1, step2));
                }

                @Test
                @DisplayName("Then - 모든 단계를 저장한다")
                void thenSavesAll() {
                    List<RecipeStep> results = recipeStepRepository.finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);
                    assertThat(results).hasSize(2);
                    assertThat(results).extracting(RecipeStep::getStepOrder).containsExactly(1, 2);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 단계 조회 (finds)")
    class Finds {

        @Nested
        @DisplayName("Given - 레시피 단계들이 저장되어 있을 때")
        class GivenSavedSteps {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                RecipeStep step2 = RecipeStep.create(2, "Step 2", List.of(), 30.0, recipeId, clock);
                RecipeStep step1 = RecipeStep.create(1, "Step 1", List.of(), 0.0, recipeId, clock);

                recipeStepRepository.create(List.of(step2, step1));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFinding {
                List<RecipeStep> results;

                @BeforeEach
                void setUp() {
                    results = recipeStepRepository.finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);
                }

                @Test
                @DisplayName("Then - 순서대로 정렬된 단계를 반환한다")
                void thenReturnsSortedSteps() {
                    assertThat(results).hasSize(2);
                    assertThat(results.get(0).getStepOrder()).isEqualTo(1);
                    assertThat(results.get(1).getStepOrder()).isEqualTo(2);
                }
            }
        }
    }
}
