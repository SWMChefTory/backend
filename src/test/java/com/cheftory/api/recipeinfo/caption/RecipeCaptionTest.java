package com.cheftory.api.recipeinfo.caption;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.caption.entity.LangCodeType;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption.Segment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCaptionTest")
public class RecipeCaptionTest {

  @Nested
  @DisplayName("레시피 캡션 생성")
  class CreateRecipeCaption {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private List<Segment> segments;
      private LangCodeType langCodeType;
      private UUID recipeId;
      private Clock clock;
      LocalDateTime now;

      @BeforeEach
      void setUp() {
        clock = mock(Clock.class);
        segments =
            List.of(
                Segment.builder().text("Hello").start(0.0).end(2.0).build(),
                Segment.builder().text("World").start(2.0).end(4.0).build());
        langCodeType = LangCodeType.ko;
        recipeId = UUID.randomUUID();
        now = LocalDateTime.now();
        doReturn(now).when(clock).now();
      }

      @Nested
      @DisplayName("When - 레시피 캡션을 생성하면")
      class WhenCreateRecipeCaption {

        private RecipeCaption recipeCaption;

        @BeforeEach
        void setUp() {
          recipeCaption = RecipeCaption.from(segments, langCodeType, recipeId, clock);
        }

        @DisplayName("Then - 레시피 캡션이 생성된다")
        @Test
        void thenRecipeCaptionIsCreated() {
          assertThat(recipeCaption).isNotNull();
          assertThat(recipeCaption.getId()).isNotNull();
          assertThat(recipeCaption.getSegments()).isEqualTo(segments);
          assertThat(recipeCaption.getLangCode()).isEqualTo(langCodeType);
          assertThat(recipeCaption.getRecipeId()).isEqualTo(recipeId);
          assertThat(recipeCaption.getCreatedAt()).isEqualTo(now);
        }
      }
    }
  }
}
