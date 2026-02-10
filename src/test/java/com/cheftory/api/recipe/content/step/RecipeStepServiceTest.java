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
    @DisplayName("레시피 단계 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파일 정보가 주어졌을 때")
        class GivenValidFileInfo {
            UUID recipeId;
            String fileUri;
            String mimeType;
            UUID stepId;

            @BeforeEach
            void setUp() throws RecipeStepException {
                recipeId = UUID.randomUUID();
                fileUri = "uri";
                mimeType = "video/mp4";
                stepId = UUID.randomUUID();

                ClientRecipeStepsResponse response = mock(ClientRecipeStepsResponse.class);
                RecipeStep step = mock(RecipeStep.class);

                doReturn(stepId).when(step).getId();
                doReturn(response).when(recipeStepClient).fetch(fileUri, mimeType);
                doReturn(List.of(step)).when(response).toRecipeSteps(recipeId, clock);
                doReturn(List.of(stepId)).when(recipeStepRepository).create(anyList());
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                List<UUID> result;

                @BeforeEach
                void setUp() throws RecipeStepException {
                    result = recipeStepService.create(recipeId, fileUri, mimeType);
                }

                @Test
                @DisplayName("Then - 외부 API로 단계를 추출하여 저장하고 ID 목록을 반환한다")
                void thenCreatesAndReturnsIds() {
                    assertThat(result).containsExactly(stepId);
                    verify(recipeStepRepository).create(anyList());
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 단계 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 레시피 ID가 주어졌을 때")
        class GivenRecipeId {
            UUID recipeId;
            List<RecipeStep> expected;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                expected = List.of(mock(RecipeStep.class));
                doReturn(expected).when(recipeStepRepository).finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeStep> result;

                @BeforeEach
                void setUp() {
                    result = recipeStepService.gets(recipeId);
                }

                @Test
                @DisplayName("Then - 정렬된 단계 목록을 반환한다")
                void thenReturnsSortedSteps() {
                    assertThat(result).isEqualTo(expected);
                    verify(recipeStepRepository).finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);
                }
            }
        }
    }
}
