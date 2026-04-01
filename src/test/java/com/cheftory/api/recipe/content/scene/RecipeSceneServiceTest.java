package com.cheftory.api.recipe.content.scene;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.scene.client.RecipeSceneClient;
import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesResponse;
import com.cheftory.api.recipe.content.scene.entity.RecipeScene;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneErrorCode;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneException;
import com.cheftory.api.recipe.content.scene.repository.RecipeSceneRepository;
import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeSceneService 테스트")
class RecipeSceneServiceTest {

    private RecipeSceneService recipeSceneService;
    private RecipeSceneRepository recipeSceneRepository;
    private RecipeSceneClient recipeSceneClient;
    private RecipeStepService recipeStepService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        recipeSceneRepository = mock(RecipeSceneRepository.class);
        recipeSceneClient = mock(RecipeSceneClient.class);
        recipeStepService = mock(RecipeStepService.class);
        clock = mock(Clock.class);
        recipeSceneService = new RecipeSceneService(recipeSceneClient, recipeSceneRepository, recipeStepService, clock);
    }

    @Nested
    @DisplayName("scene 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 step과 파일 정보가 주어졌을 때")
        class GivenValidInput {
            UUID recipeId;
            String fileUri;
            String mimeType;
            UUID sceneId;
            List<RecipeStep> recipeSteps;

            @BeforeEach
            void setUp() throws RecipeSceneException {
                recipeId = UUID.randomUUID();
                fileUri = "uri";
                mimeType = "video/mp4";
                sceneId = UUID.randomUUID();
                recipeSteps = List.of(mock(RecipeStep.class));

                ClientRecipeScenesResponse response = mock(ClientRecipeScenesResponse.class);
                RecipeScene scene = mock(RecipeScene.class);

                doReturn(sceneId).when(scene).getId();
                doReturn(recipeSteps).when(recipeStepService).gets(recipeId);
                doReturn(response).when(recipeSceneClient).fetch(fileUri, mimeType, recipeSteps);
                doReturn(List.of(scene)).when(response).toRecipeScenes(recipeId, clock);
                doReturn(List.of(sceneId)).when(recipeSceneRepository).create(anyList());
            }

            @Test
            @DisplayName("Then - step 기반으로 scene을 추출해 저장하고 ID 목록을 반환한다")
            void thenCreatesAndReturnsIds() throws RecipeSceneException {
                List<UUID> result = recipeSceneService.create(recipeId, fileUri, mimeType);

                assertThat(result).containsExactly(sceneId);
                verify(recipeSceneRepository).create(anyList());
            }
        }

        @Nested
        @DisplayName("Given - 저장된 step이 없을 때")
        class GivenNoSteps {

            @Test
            @DisplayName("Then - scene 생성 실패 예외를 던지고 외부 호출을 하지 않는다")
            void thenThrowsException() {
                UUID recipeId = UUID.randomUUID();
                doReturn(List.of()).when(recipeStepService).gets(recipeId);

                assertThatThrownBy(() -> recipeSceneService.create(recipeId, "uri", "video/mp4"))
                        .isInstanceOf(RecipeSceneException.class)
                        .hasFieldOrPropertyWithValue("error", RecipeSceneErrorCode.RECIPE_SCENE_CREATE_FAIL);

                verifyNoInteractions(recipeSceneClient);
            }
        }

        @Nested
        @DisplayName("Given - 응답에 step_id가 없을 때")
        class GivenLegacySceneResponse {

            @Test
            @DisplayName("Then - strict step_id 정책으로 scene 생성 실패 예외를 던진다")
            void thenThrowsException() throws RecipeSceneException {
                UUID recipeId = UUID.randomUUID();
                List<RecipeStep> recipeSteps = List.of(mock(RecipeStep.class));
                ClientRecipeScenesResponse response = new ClientRecipeScenesResponse(
                        List.of(new ClientRecipeScenesResponse.Scene(null, "양파썰기", 1.0, 5.0, 8)));

                doReturn(recipeSteps).when(recipeStepService).gets(recipeId);
                doReturn(response).when(recipeSceneClient).fetch("uri", "video/mp4", recipeSteps);

                assertThatThrownBy(() -> recipeSceneService.create(recipeId, "uri", "video/mp4"))
                        .isInstanceOf(RecipeSceneException.class)
                        .hasFieldOrPropertyWithValue("error", RecipeSceneErrorCode.RECIPE_SCENE_CREATE_FAIL);
            }
        }
    }

    @Nested
    @DisplayName("scene 존재 여부 확인 (exists)")
    class Exists {

        @Test
        @DisplayName("scene이 존재하면 true를 반환한다")
        void returnsTrueWhenExists() {
            UUID recipeId = UUID.randomUUID();
            doReturn(true).when(recipeSceneRepository).existsByRecipeId(recipeId);

            boolean result = recipeSceneService.exists(recipeId);

            assertThat(result).isTrue();
            verify(recipeSceneRepository).existsByRecipeId(recipeId);
        }

        @Test
        @DisplayName("scene이 없으면 false를 반환한다")
        void returnsFalseWhenNotExists() {
            UUID recipeId = UUID.randomUUID();
            doReturn(false).when(recipeSceneRepository).existsByRecipeId(recipeId);

            boolean result = recipeSceneService.exists(recipeId);

            assertThat(result).isFalse();
            verify(recipeSceneRepository).existsByRecipeId(recipeId);
        }
    }
}
