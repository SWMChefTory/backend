package com.cheftory.api.recipe.content.ingredient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeIngredientTest")
public class RecipeIngredientTest {

    @Nested
    @DisplayName("레시피 재료 생성")
    class CreateRecipeIngredient {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {

            private String name;
            private String unit;
            private UUID recipeId;
            private Clock clock;
            private Integer amount;
            private LocalDateTime now;

            @BeforeEach
            void setUp() {
                name = "Sugar";
                unit = "grams";
                amount = 100;
                recipeId = UUID.randomUUID();
                clock = mock(Clock.class);
                now = LocalDateTime.now();
                doReturn(now).when(clock).now();
            }

            @Nested
            @DisplayName("When - 레시피 재료를 생성하면")
            class WhenCreateRecipeIngredient {

                private RecipeIngredient recipeIngredient;

                @BeforeEach
                void setUp() {
                    recipeIngredient = RecipeIngredient.create(name, unit, amount, recipeId, clock);
                }

                @DisplayName("Then - 레시피 재료가 생성된다")
                @Test
                void thenRecipeIngredientIsCreated() {
                    assertThat(recipeIngredient).isNotNull();
                    assertThat(recipeIngredient.getId()).isNotNull();
                    assertThat(recipeIngredient.getName()).isEqualTo(name);
                    assertThat(recipeIngredient.getUnit()).isEqualTo(unit);
                    assertThat(recipeIngredient.getAmount()).isEqualTo(amount);
                    assertThat(recipeIngredient.getRecipeId()).isEqualTo(recipeId);
                    assertThat(recipeIngredient.getCreatedAt()).isEqualTo(now);
                }
            }
        }
    }
}
