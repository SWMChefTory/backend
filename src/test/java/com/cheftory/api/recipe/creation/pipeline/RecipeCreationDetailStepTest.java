package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.content.detail.RecipeDetailService;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.ingredient.RecipeIngredientService;
import com.cheftory.api.recipe.content.tag.RecipeTagService;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

@DisplayName("RecipeCreationDetailStep 테스트")
class RecipeCreationDetailStepTest {

    private RecipeDetailService recipeDetailService;
    private RecipeIngredientService recipeIngredientService;
    private RecipeTagService recipeTagService;
    private RecipeDetailMetaService recipeDetailMetaService;
    private RecipeProgressService recipeProgressService;
    private RecipeCreationDetailStep sut;

    @BeforeEach
    void setUp() {
        recipeDetailService = mock(RecipeDetailService.class);
        recipeIngredientService = mock(RecipeIngredientService.class);
        recipeTagService = mock(RecipeTagService.class);
        recipeDetailMetaService = mock(RecipeDetailMetaService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        sut = createStep();
    }

    private RecipeCreationDetailStep createStep() {
        try {
            Constructor<RecipeCreationDetailStep> ctor = RecipeCreationDetailStep.class.getDeclaredConstructor(
                    RecipeDetailService.class,
                    RecipeIngredientService.class,
                    RecipeTagService.class,
                    RecipeDetailMetaService.class,
                    RecipeProgressService.class);
            ctor.setAccessible(true);
            return ctor.newInstance(
                    recipeDetailService,
                    recipeIngredientService,
                    recipeTagService,
                    recipeDetailMetaService,
                    recipeProgressService);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RecipeCreationDetailStep", ex);
        }
    }

    @Nested
    @DisplayName("실행 (run)")
    class Run {

        @Nested
        @DisplayName("Given - 파일 정보가 없을 때")
        class GivenNoFileInfo {
            RecipeCreationExecutionContext context;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.of(recipeId, "video-123", URI.create("https://youtu.be/video-123"), "test-title");
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @Test
                @DisplayName("Then - RECIPE_CREATE_FAIL 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> sut.run(context))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_CREATE_FAIL);

                    verify(recipeProgressService, never())
                            .start(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT);
                }
            }
        }

        @Nested
        @DisplayName("Given - 파일 정보가 있고 정상 동작할 때")
        class GivenFileInfoAndSuccess {
            RecipeCreationExecutionContext context;
            UUID recipeId;
            String videoId;
            String fileUri;
            String mimeType;
            RecipeDetail detail;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                videoId = "video-456";
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, videoId, URI.create("https://youtu.be/video-456"), "youtube-original-title"),
                        fileUri,
                        mimeType);

                detail = RecipeDetail.of("ai-generated-title",
                        "desc", List.of(RecipeDetail.Ingredient.of("salt", 1, "tsp")), List.of("tag1"), 2, 10);
                when(recipeDetailService.getRecipeDetails(videoId, fileUri, mimeType, "youtube-original-title"))
                        .thenReturn(detail);
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @BeforeEach
                void setUp() throws RecipeException {
                    sut.run(context);
                }

                @Test
                @DisplayName("Then - 상세 정보를 생성하고 진행 상태를 업데이트한다")
                void thenCreatesDetailAndUpdatesProgress() {
                    InOrder order = inOrder(recipeProgressService);
                    order.verify(recipeProgressService)
                            .start(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META);

                    verify(recipeIngredientService).create(recipeId, detail.ingredients());
                    verify(recipeTagService).create(recipeId, detail.tags());
                    verify(recipeDetailMetaService)
                            .create(recipeId, detail.cookTime(), detail.servings(), detail.description(), "ai-generated-title");
                }
            }
        }

        @Nested
        @DisplayName("Given - 상세 정보 생성 중 예외가 발생할 때")
        class GivenException {
            RecipeCreationExecutionContext context;
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                String videoId = "video-789";
                String fileUri = "s3://bucket/file.mp4";
                String mimeType = "video/mp4";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, videoId, URI.create("https://youtu.be/video-789"), "test-title"),
                        fileUri,
                        mimeType);

                when(recipeDetailService.getRecipeDetails(videoId, fileUri, mimeType, "test-title"))
                        .thenThrow(new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL));
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @Test
                @DisplayName("Then - 진행 상태를 실패로 기록하고 예외를 던진다")
                void thenFailsProgressAndThrowsException() {
                    assertThatThrownBy(() -> sut.run(context))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_CREATE_FAIL);

                    verify(recipeProgressService)
                            .failed(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META);
                    verify(recipeIngredientService, never()).create(ArgumentMatchers.any(), ArgumentMatchers.anyList());
                }
            }
        }
    }
}
