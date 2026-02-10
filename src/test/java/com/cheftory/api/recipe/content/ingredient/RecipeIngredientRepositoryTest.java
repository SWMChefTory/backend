package com.cheftory.api.recipe.content.ingredient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.ingredient.repository.RecipeIngredientRepository;
import com.cheftory.api.recipe.content.ingredient.repository.RecipeIngredientRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({RecipeIngredientRepositoryImpl.class})
@DisplayName("RecipeIngredientRepository 테스트")
class RecipeIngredientRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    private Clock clock;
    private final LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("레시피 재료 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 여러 재료가 주어졌을 때")
        class GivenMultipleIngredients {
            UUID recipeId;
            RecipeIngredient ingredient1;
            RecipeIngredient ingredient2;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                ingredient1 = RecipeIngredient.create("재료1", "g", 100, recipeId, clock);
                ingredient2 = RecipeIngredient.create("재료2", "ml", 200, recipeId, clock);
            }

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenSaving {

                @BeforeEach
                void setUp() {
                    recipeIngredientRepository.create(List.of(ingredient1, ingredient2));
                }

                @Test
                @DisplayName("Then - 모든 재료를 저장한다")
                void thenSavesAll() {
                    List<RecipeIngredient> results = recipeIngredientRepository.finds(recipeId);
                    assertThat(results).hasSize(2);
                    assertThat(results).extracting(RecipeIngredient::getName).containsExactlyInAnyOrder("재료1", "재료2");
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 재료 조회 (finds)")
    class Finds {

        @Nested
        @DisplayName("Given - 재료가 저장되어 있을 때")
        class GivenSavedIngredients {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                RecipeIngredient ingredient = RecipeIngredient.create("재료", "g", 100, recipeId, clock);
                recipeIngredientRepository.create(List.of(ingredient));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFinding {
                List<RecipeIngredient> results;

                @BeforeEach
                void setUp() {
                    results = recipeIngredientRepository.finds(recipeId);
                }

                @Test
                @DisplayName("Then - 해당 레시피의 모든 재료를 반환한다")
                void thenReturnsAll() {
                    assertThat(results).hasSize(1);
                    assertThat(results.getFirst().getRecipeId()).isEqualTo(recipeId);
                }
            }
        }
    }
}
