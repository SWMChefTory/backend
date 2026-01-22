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

@DisplayName("RecipeDetailMetaTest")
public class RecipeDetailMetaTest {

    @Nested
    @DisplayName("레시피 상세 메타 생성")
    class CreateRecipeDetailMeta {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {

            private Integer cookingTime;
            private Integer servings;
            private String description;
            private Clock clock;
            private UUID recipeId;
            private LocalDateTime now;

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
            @DisplayName("When - 레시피 상세 메타를 생성하면")
            class WhenCreateRecipeDetailMeta {

                private RecipeDetailMeta recipeDetailMeta;

                @BeforeEach
                void setUp() {
                    recipeDetailMeta = RecipeDetailMeta.create(cookingTime, servings, description, clock, recipeId);
                }

                @DisplayName("Then - 레시피 상세 메타가 생성된다")
                @Test
                void thenRecipeDetailMetaIsCreated() {
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
