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
            UUID jobId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.of(recipeId, "video-123", "test-title", jobId);
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
                            .start(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT, jobId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 파일 정보가 있고 정상 동작할 때")
        class GivenFileInfoAndSuccess {
            RecipeCreationExecutionContext context;
            UUID recipeId;
            String videoId;
            UUID jobId;
            String fileUri;
            String mimeType;
            RecipeDetail detail;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                videoId = "video-456";
                jobId = UUID.randomUUID();
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, videoId, "youtube-original-title", jobId),
                        fileUri,
                        mimeType);

                detail = RecipeDetail.of(
                        "ai-generated-title",
                        "desc",
                        List.of(RecipeDetail.Ingredient.of("salt", 1, "tsp")),
                        List.of("tag1"),
                        2,
                        10);
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
                            .start(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT, jobId);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT, jobId);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG, jobId);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META, jobId);

                    verify(recipeIngredientService).create(recipeId, detail.ingredients());
                    verify(recipeTagService).create(recipeId, detail.tags());
                    verify(recipeDetailMetaService)
                            .create(
                                    recipeId,
                                    detail.cookTime(),
                                    detail.servings(),
                                    detail.description(),
                                    "ai-generated-title");
                }
            }
        }

        @Nested
        @DisplayName("Given - ingredient/tag/detailMeta가 모두 이미 존재할 때")
        class GivenAllArtifactsExist {
            RecipeCreationExecutionContext context;
            UUID recipeId;
            UUID jobId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, "video-skip", "test-title", jobId),
                        "s3://bucket/file.mp4",
                        "video/mp4");
                when(recipeIngredientService.exists(recipeId)).thenReturn(true);
                when(recipeTagService.exists(recipeId)).thenReturn(true);
                when(recipeDetailMetaService.exists(recipeId)).thenReturn(true);
            }

            @Test
            @DisplayName("Then - detail 생성 호출을 생략하고 success만 기록한다")
            void thenSkipDetailGeneration() throws Exception {
                RecipeCreationExecutionContext result = sut.run(context);

                org.assertj.core.api.Assertions.assertThat(result).isEqualTo(context);
                verify(recipeDetailService, never())
                        .getRecipeDetails(
                                ArgumentMatchers.any(),
                                ArgumentMatchers.any(),
                                ArgumentMatchers.any(),
                                ArgumentMatchers.any());
                verify(recipeProgressService)
                        .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META, jobId);
                verify(recipeProgressService, never())
                        .start(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT, jobId);
            }
        }

        @Nested
        @DisplayName("Given - 일부 결과물만 존재할 때")
        class GivenPartialArtifactsExist {
            RecipeCreationExecutionContext context;
            UUID recipeId;
            UUID jobId;
            RecipeDetail detail;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, "video-partial", "title", jobId),
                        "s3://bucket/file.mp4",
                        "video/mp4");
                when(recipeIngredientService.exists(recipeId)).thenReturn(true);
                when(recipeTagService.exists(recipeId)).thenReturn(false);
                when(recipeDetailMetaService.exists(recipeId)).thenReturn(true);
                detail = RecipeDetail.of(
                        "generated-title",
                        "desc",
                        List.of(RecipeDetail.Ingredient.of("salt", 1, "tsp")),
                        List.of("tag1"),
                        1,
                        5);
                when(recipeDetailService.getRecipeDetails(
                                "video-partial", "s3://bucket/file.mp4", "video/mp4", "title"))
                        .thenReturn(detail);
            }

            @Test
            @DisplayName("Then - skip하지 않고 상세 생성을 수행한다")
            void thenGenerate() throws Exception {
                sut.run(context);

                verify(recipeDetailService)
                        .getRecipeDetails("video-partial", "s3://bucket/file.mp4", "video/mp4", "title");
            }
        }

        @Nested
        @DisplayName("Given - 상세 정보 생성 중 예외가 발생할 때")
        class GivenException {
            RecipeCreationExecutionContext context;
            UUID recipeId;
            UUID jobId;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
                String videoId = "video-789";
                String fileUri = "s3://bucket/file.mp4";
                String mimeType = "video/mp4";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, videoId, "test-title", jobId), fileUri, mimeType);

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
                            .failed(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META, jobId);
                    verify(recipeIngredientService, never()).create(ArgumentMatchers.any(), ArgumentMatchers.anyList());
                }
            }
        }
    }
}
