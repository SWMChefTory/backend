package com.cheftory.api.recipe.content.detailmeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeDetailMeta 엔티티")
public class RecipeDetailMetaTest {

    @Nested
    @DisplayName("상세 메타 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            Integer cookingTime;
            Integer servings;
            String description;
            Clock clock;
            UUID recipeId;
            LocalDateTime now;

            @BeforeEach
            void setUp() {
                cookingTime = 30; // in minutes
                servings = 4;
                description = "A delicious recipe for testing.";
                recipeId = UUID.randomUUID();
                clock = mock(Clock.class);
                now = LocalDateTime.now();
                doReturn(now).when(clock).now();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeDetailMeta recipeDetailMeta;

                @BeforeEach
                void setUp() {
                    recipeDetailMeta =
                            RecipeDetailMeta.create(cookingTime, servings, description, null, clock, recipeId);
                }

                @Test
                @DisplayName("Then - 상세 메타가 올바르게 생성된다")
                void thenCreatedCorrectly() {
                    assertThat(recipeDetailMeta).isNotNull();
                    assertThat(recipeDetailMeta.getId()).isNotNull();
                    assertThat(recipeDetailMeta.getCookTime()).isEqualTo(cookingTime);
                    assertThat(recipeDetailMeta.getServings()).isEqualTo(servings);
                    assertThat(recipeDetailMeta.getDescription()).isEqualTo(description);
                    assertThat(recipeDetailMeta.getRecipeId()).isEqualTo(recipeId);
                    assertThat(recipeDetailMeta.getCreatedAt()).isEqualTo(now);
                }
            }
        }
    }
}
