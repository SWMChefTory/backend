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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
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
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("여러 레시피 단계를 한 번에 저장한다")
        void it_saves_multiple_steps() {
            // Given
            UUID recipeId = UUID.randomUUID();
            RecipeStep step1 = RecipeStep.create(1, "Step 1", List.of(), 0.0, recipeId, clock);
            RecipeStep step2 = RecipeStep.create(2, "Step 2", List.of(), 30.0, recipeId, clock);

            // When
            recipeStepRepository.create(List.of(step1, step2));

            // Then
            List<RecipeStep> results = recipeStepRepository.finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);
            assertThat(results).hasSize(2);
            assertThat(results).extracting(RecipeStep::getStepOrder).containsExactly(1, 2);
        }
    }

    @Nested
    @DisplayName("findAllByRecipeId 메서드는")
    class Describe_findAllByRecipeId {

        @Test
        @DisplayName("특정 레시피의 모든 단계를 정렬하여 조회한다")
        void it_returns_sorted_steps_for_recipe() {
            // Given
            UUID recipeId = UUID.randomUUID();
            RecipeStep step2 = RecipeStep.create(2, "Step 2", List.of(), 30.0, recipeId, clock);
            RecipeStep step1 = RecipeStep.create(1, "Step 1", List.of(), 0.0, recipeId, clock);

            recipeStepRepository.create(List.of(step2, step1));

            // When
            List<RecipeStep> results = recipeStepRepository.finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getStepOrder()).isEqualTo(1);
            assertThat(results.get(1).getStepOrder()).isEqualTo(2);
        }
    }
}
