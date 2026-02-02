package com.cheftory.api.recipe.creation.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressState;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("RecipeProgress")
class RecipeProgressTest {

    private Clock clock;
    private LocalDateTime now;
    private UUID recipeId;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        now = LocalDateTime.now();
        when(clock.now()).thenReturn(now);
        recipeId = UUID.randomUUID();
    }

    @ParameterizedTest(name = "Step={0} 에 대해 생성 성공")
    @EnumSource(RecipeProgressStep.class)
    void create_with_all_steps(RecipeProgressStep step) {
        RecipeProgressDetail detail = pickDefaultDetailFor(step);

        RecipeProgress progress = RecipeProgress.create(recipeId, clock, step, detail, RecipeProgressState.SUCCESS);

        assertThat(progress).isNotNull();
        assertThat(ReflectionTestUtils.getField(progress, "id")).isNotNull();
        assertThat(ReflectionTestUtils.getField(progress, "recipeId")).isEqualTo(recipeId);
        assertThat(ReflectionTestUtils.getField(progress, "step")).isEqualTo(step);
        assertThat(ReflectionTestUtils.getField(progress, "detail")).isEqualTo(detail);
        assertThat(ReflectionTestUtils.getField(progress, "state")).isEqualTo(RecipeProgressState.SUCCESS);
        assertThat(ReflectionTestUtils.getField(progress, "createdAt")).isEqualTo(now);
    }

    @ParameterizedTest(name = "State={0} 에 대해 생성 성공")
    @EnumSource(RecipeProgressState.class)
    void create_with_all_states(RecipeProgressState state) {
        RecipeProgressStep step = RecipeProgressStep.READY;
        RecipeProgressDetail detail = RecipeProgressDetail.READY;

        RecipeProgress progress = RecipeProgress.create(recipeId, clock, step, detail, state);

        assertThat(progress).isNotNull();
        assertThat(ReflectionTestUtils.getField(progress, "step")).isEqualTo(step);
        assertThat(ReflectionTestUtils.getField(progress, "detail")).isEqualTo(detail);
        assertThat(ReflectionTestUtils.getField(progress, "state")).isEqualTo(state);
        assertThat(ReflectionTestUtils.getField(progress, "createdAt")).isEqualTo(now);
    }

    @ParameterizedTest(name = "Detail={0} 에 대해 생성 성공")
    @EnumSource(RecipeProgressDetail.class)
    void create_with_all_details(RecipeProgressDetail detail) {
        RecipeProgressStep step = pickDefaultStepFor(detail);
        RecipeProgressState state = RecipeProgressState.SUCCESS;

        RecipeProgress progress = RecipeProgress.create(recipeId, clock, step, detail, state);

        assertThat(progress).isNotNull();
        assertThat(ReflectionTestUtils.getField(progress, "step")).isEqualTo(step);
        assertThat(ReflectionTestUtils.getField(progress, "detail")).isEqualTo(detail);
        assertThat(ReflectionTestUtils.getField(progress, "state")).isEqualTo(state);
        assertThat(ReflectionTestUtils.getField(progress, "createdAt")).isEqualTo(now);
    }

    @Test
    @DisplayName("RUNNING 상태로 생성")
    void create_with_running_state() {
        RecipeProgressStep step = RecipeProgressStep.CAPTION;
        RecipeProgressDetail detail = RecipeProgressDetail.CAPTION;

        RecipeProgress progress = RecipeProgress.create(recipeId, clock, step, detail, RecipeProgressState.RUNNING);

        assertThat(ReflectionTestUtils.getField(progress, "state")).isEqualTo(RecipeProgressState.RUNNING);
        assertThat(ReflectionTestUtils.getField(progress, "step")).isEqualTo(step);
        assertThat(ReflectionTestUtils.getField(progress, "detail")).isEqualTo(detail);
    }

    @Test
    @DisplayName("SUCCESS 상태로 생성")
    void create_with_success_state() {
        RecipeProgressStep step = RecipeProgressStep.CAPTION;
        RecipeProgressDetail detail = RecipeProgressDetail.CAPTION;

        RecipeProgress progress = RecipeProgress.create(recipeId, clock, step, detail, RecipeProgressState.SUCCESS);

        assertThat(ReflectionTestUtils.getField(progress, "state")).isEqualTo(RecipeProgressState.SUCCESS);
        assertThat(ReflectionTestUtils.getField(progress, "step")).isEqualTo(step);
        assertThat(ReflectionTestUtils.getField(progress, "detail")).isEqualTo(detail);
    }

    @Test
    @DisplayName("FAILED 상태로 생성")
    void create_with_failed_state() {
        RecipeProgressStep step = RecipeProgressStep.CAPTION;
        RecipeProgressDetail detail = RecipeProgressDetail.CAPTION;

        RecipeProgress progress = RecipeProgress.create(recipeId, clock, step, detail, RecipeProgressState.FAILED);

        assertThat(ReflectionTestUtils.getField(progress, "state")).isEqualTo(RecipeProgressState.FAILED);
        assertThat(ReflectionTestUtils.getField(progress, "step")).isEqualTo(step);
        assertThat(ReflectionTestUtils.getField(progress, "detail")).isEqualTo(detail);
    }

    private RecipeProgressStep pickDefaultStepFor(RecipeProgressDetail detail) {
        return switch (detail) {
            case READY -> RecipeProgressStep.READY;
            case CAPTION -> RecipeProgressStep.CAPTION;
            case STEP -> RecipeProgressStep.STEP;
            case FINISHED -> RecipeProgressStep.FINISHED;
            case BRIEFING -> RecipeProgressStep.BRIEFING;
            case TAG, DETAIL_META, INGREDIENT, DETAIL -> RecipeProgressStep.DETAIL;
        };
    }

    private RecipeProgressDetail pickDefaultDetailFor(RecipeProgressStep step) {
        return switch (step) {
            case READY -> RecipeProgressDetail.READY;
            case CAPTION -> RecipeProgressDetail.CAPTION;
            case DETAIL -> RecipeProgressDetail.DETAIL;
            case STEP -> RecipeProgressDetail.STEP;
            case FINISHED -> RecipeProgressDetail.FINISHED;
            case BRIEFING -> RecipeProgressDetail.BRIEFING;
        };
    }
}
