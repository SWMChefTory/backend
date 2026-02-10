package com.cheftory.api.recipe.content.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeTag 엔티티")
public class RecipeTagTest {

    @Nested
    @DisplayName("레시피 태그 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            String tag;
            UUID recipeId;
            Clock clock;
            LocalDateTime now;

            @BeforeEach
            void setUp() {
                tag = "한식";
                recipeId = UUID.randomUUID();
                clock = mock(Clock.class);
                now = LocalDateTime.now();
                doReturn(now).when(clock).now();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeTag recipeTag;

                @BeforeEach
                void setUp() {
                    recipeTag = RecipeTag.create(tag, recipeId, clock);
                }

                @Test
                @DisplayName("Then - 레시피 태그가 올바르게 생성된다")
                void thenCreatedCorrectly() {
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
