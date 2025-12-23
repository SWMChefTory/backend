package com.cheftory.api.recipe.creation.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

    RecipeProgress progress = RecipeProgress.create(recipeId, clock, step, detail);

    assertThat(progress).isNotNull();
    assertThat(progress.getId()).isNotNull();
    assertThat(progress.getRecipeId()).isEqualTo(recipeId);
    assertThat(progress.getStep()).isEqualTo(step);
    assertThat(progress.getDetail()).isEqualTo(detail);
    assertThat(progress.getCreatedAt()).isEqualTo(now);
  }

  @ParameterizedTest(name = "Detail={0} 에 대해 생성 성공")
  @EnumSource(RecipeProgressDetail.class)
  void create_with_all_details(RecipeProgressDetail detail) {
    RecipeProgressStep step = pickDefaultStepFor(detail);

    RecipeProgress progress = RecipeProgress.create(recipeId, clock, step, detail);

    assertThat(progress).isNotNull();
    assertThat(progress.getStep()).isEqualTo(step);
    assertThat(progress.getDetail()).isEqualTo(detail);
    assertThat(progress.getCreatedAt()).isEqualTo(now);
  }

  private RecipeProgressStep pickDefaultStepFor(RecipeProgressDetail detail) {
    return switch (detail) {
      case READY -> RecipeProgressStep.READY;
      case CAPTION -> RecipeProgressStep.CAPTION;
      case STEP -> RecipeProgressStep.STEP;
      case FINISHED -> RecipeProgressStep.FINISHED;
      case BRIEFING -> RecipeProgressStep.BRIEFING;
      case TAG, DETAIL_META, INGREDIENT -> RecipeProgressStep.DETAIL;
    };
  }

  private RecipeProgressDetail pickDefaultDetailFor(RecipeProgressStep step) {
    return switch (step) {
      case READY -> RecipeProgressDetail.READY;
      case CAPTION -> RecipeProgressDetail.CAPTION;
      case DETAIL -> RecipeProgressDetail.DETAIL_META;
      case STEP -> RecipeProgressDetail.STEP;
      case FINISHED -> RecipeProgressDetail.FINISHED;
      case BRIEFING -> RecipeProgressDetail.BRIEFING;
    };
  }
}
