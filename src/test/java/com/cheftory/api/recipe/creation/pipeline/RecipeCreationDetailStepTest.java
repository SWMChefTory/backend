package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
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

@DisplayName("RecipeCreationDetailStep")
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
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("caption이 없으면 RECIPE_CREATE_FAIL 예외가 발생한다")
        void shouldThrowWhenCaptionMissing() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-123";
            URI videoUrl = URI.create("https://youtu.be/video-123");
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);

            assertThatThrownBy(() -> sut.run(context))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_CREATE_FAIL);

            verify(recipeProgressService, never())
                    .start(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL);
        }

        @Test
        @DisplayName("성공 시 detail과 progress가 순서대로 기록된다")
        void shouldCreateDetailAndProgress() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-456";
            URI videoUrl = URI.create("https://youtu.be/video-456");
            RecipeCaption caption = mock(RecipeCaption.class);
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.from(
                    RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl), caption);

            RecipeDetail detail = RecipeDetail.of(
                    "desc", List.of(RecipeDetail.Ingredient.of("salt", 1, "tsp")), List.of("tag1"), 2, 10);
            when(recipeDetailService.getRecipeDetails(videoId, caption)).thenReturn(detail);

            sut.run(context);

            InOrder order = inOrder(recipeProgressService);
            order.verify(recipeProgressService).start(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL);
            order.verify(recipeProgressService)
                    .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT);
            order.verify(recipeProgressService).success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG);
            order.verify(recipeProgressService)
                    .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META);
            order.verify(recipeProgressService)
                    .success(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL);

            verify(recipeIngredientService).create(recipeId, detail.ingredients());
            verify(recipeTagService).create(recipeId, detail.tags());
            verify(recipeDetailMetaService)
                    .create(recipeId, detail.cookTime(), detail.servings(), detail.description());
        }

        @Test
        @DisplayName("예외 발생 시 progress를 failed로 기록한다")
        void shouldFailProgressWhenExceptionThrown() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-789";
            URI videoUrl = URI.create("https://youtu.be/video-789");
            RecipeCaption caption = mock(RecipeCaption.class);
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.from(
                    RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl), caption);

            when(recipeDetailService.getRecipeDetails(videoId, caption))
                    .thenThrow(new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL));

            assertThatThrownBy(() -> sut.run(context))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_CREATE_FAIL);

            verify(recipeProgressService).failed(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL);
            verify(recipeIngredientService, never()).create(ArgumentMatchers.any(), ArgumentMatchers.anyList());
        }
    }
}
