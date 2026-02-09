package com.cheftory.api.recipe.content.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.step.client.RecipeStepClient;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.step.entity.RecipeStepSort;
import com.cheftory.api.recipe.content.step.exception.RecipeStepException;
import com.cheftory.api.recipe.content.step.repository.RecipeStepRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeStepService 테스트")
class RecipeStepServiceTest {

    private RecipeStepService recipeStepService;
    private RecipeStepRepository recipeStepRepository;
    private RecipeStepClient recipeStepClient;
    private Clock clock;

    @BeforeEach
    void setUp() {
        recipeStepRepository = mock(RecipeStepRepository.class);
        recipeStepClient = mock(RecipeStepClient.class);
        clock = mock(Clock.class);
        recipeStepService = new RecipeStepService(recipeStepClient, recipeStepRepository, clock);
    }

    @Nested
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("외부 API로부터 단계를 추출하여 저장하고 ID 목록을 반환한다")
        void it_creates_steps_and_returns_ids() throws RecipeStepException {
            // Given
            UUID recipeId = UUID.randomUUID();
            String fileUri = "uri";
            String mimeType = "video/mp4";

            ClientRecipeStepsResponse response = mock(ClientRecipeStepsResponse.class);
            RecipeStep step = mock(RecipeStep.class);
            UUID stepId = UUID.randomUUID();

            doReturn(stepId).when(step).getId();
            doReturn(response).when(recipeStepClient).fetch(fileUri, mimeType);
            doReturn(List.of(step)).when(response).toRecipeSteps(recipeId, clock);
            doReturn(List.of(stepId)).when(recipeStepRepository).create(anyList());

            // When
            List<UUID> result = recipeStepService.create(recipeId, fileUri, mimeType);

            // Then
            assertThat(result).containsExactly(stepId);
            verify(recipeStepRepository).create(anyList());
        }
    }

    @Nested
    @DisplayName("gets 메서드는")
    class Describe_gets {

        @Test
        @DisplayName("레시피 ID로 정렬된 단계 목록을 조회한다")
        void it_returns_sorted_steps_by_recipe_id() {
            // Given
            UUID recipeId = UUID.randomUUID();
            List<RecipeStep> expected = List.of(mock(RecipeStep.class));
            doReturn(expected).when(recipeStepRepository).finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);

            // When
            List<RecipeStep> result = recipeStepService.gets(recipeId);

            // Then
            assertThat(result).isEqualTo(expected);
            verify(recipeStepRepository).finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);
        }
    }
}
