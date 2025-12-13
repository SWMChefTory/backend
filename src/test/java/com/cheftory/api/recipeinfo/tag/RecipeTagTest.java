package com.cheftory.api.recipeinfo.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.tag.entity.RecipeTag;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeTagTest")
public class RecipeTagTest {

  @Nested
  @DisplayName("레시피 태그 생성")
  class CreateRecipeTag {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private String tag;
      private UUID recipeId;
      private Clock clock;
      private LocalDateTime now;

      @BeforeEach
      void setUp() {
        tag = "한식";
        recipeId = UUID.randomUUID();
        clock = mock(Clock.class);
        now = LocalDateTime.now();
        doReturn(now).when(clock).now();
      }

      @Nested
      @DisplayName("When - 레시피 태그를 생성하면")
      class WhenCreateRecipeTag {

        private RecipeTag recipeTag;

        @BeforeEach
        void setUp() {
          recipeTag = RecipeTag.create(tag, recipeId, clock);
        }

        @DisplayName("Then - 레시피 태그가 생성된다")
        @Test
        void thenRecipeTagIsCreated() {
          assertThat(recipeTag).isNotNull();
          assertThat(recipeTag.getId()).isNotNull();
          assertThat(recipeTag.getTag()).isEqualTo(tag);
          assertThat(recipeTag.getRecipeId()).isEqualTo(recipeId);
          assertThat(recipeTag.getCreatedAt()).isEqualTo(now);
        }
      }
    }
  }
}
