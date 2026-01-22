package com.cheftory.api.recipe.content.ingredient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailResponse.Ingredient;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeIngredientRepositoryTest")
public class RecipeIngredientRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @MockitoBean
    private Clock clock;

    @Nested
    @DisplayName("레시피 재료 저장")
    class SaveIngredients {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {

            private UUID recipeId;
            private List<Ingredient> ingredients;
            private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

            @BeforeEach
            void setUp() {
                clock = mock(Clock.class);
                recipeId = UUID.randomUUID();
                ingredients = List.of(new Ingredient("재료1", 100, "g"), new Ingredient("재료2", 200, "ml"));
                doReturn(FIXED_TIME).when(clock).now();
            }

            @DisplayName("When - 레시피 재료를 저장하면")
            @Nested
            class WhenSaveIngredients {

                private List<RecipeIngredient> recipeIngredients;

                @BeforeEach
                void setUp() {
                    recipeIngredients = ingredients.stream()
                            .map(ingredient -> RecipeIngredient.create(
                                    ingredient.name(), ingredient.unit(), ingredient.amount(), recipeId, clock))
                            .toList();
                    recipeIngredientRepository.saveAll(recipeIngredients);
                }

                @DisplayName("Then - 레시피 재료가 저장된다")
                @Test
                void thenIngredientsSaved() {
                    List<RecipeIngredient> foundIngredients = recipeIngredientRepository.findAllByRecipeId(recipeId);
                    assertThat(foundIngredients).hasSize(2);
                    assertThat(foundIngredients).extracting("name").containsExactlyInAnyOrder("재료1", "재료2");
                    assertThat(foundIngredients).extracting("amount").containsExactlyInAnyOrder(100, 200);
                    assertThat(foundIngredients).extracting("unit").containsExactlyInAnyOrder("g", "ml");
                    assertThat(foundIngredients).extracting("createdAt").containsOnly(FIXED_TIME);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 재료 조회")
    class FindIngredients {

        @Nested
        @DisplayName("Given - 저장된 레시피 재료가 있을 때")
        class GivenSavedIngredients {
            private UUID recipeId;
            private List<Ingredient> ingredients;
            private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

            @BeforeEach
            void setUp() {
                clock = mock(Clock.class);
                recipeId = UUID.randomUUID();
                ingredients = List.of(new Ingredient("재료1", 100, "g"), new Ingredient("재료2", 200, "ml"));
                doReturn(FIXED_TIME).when(clock).now();

                List<RecipeIngredient> recipeIngredients = ingredients.stream()
                        .map(ingredient -> RecipeIngredient.create(
                                ingredient.name(), ingredient.unit(), ingredient.amount(), recipeId, clock))
                        .toList();
                recipeIngredientRepository.saveAll(recipeIngredients);
            }

            @DisplayName("When - 레시피 ID로 레시피 재료를 조회하면")
            @Nested
            class WhenFindIngredientsByRecipeId {

                private List<RecipeIngredient> foundIngredients;

                @BeforeEach
                void setUp() {
                    foundIngredients = recipeIngredientRepository.findAllByRecipeId(recipeId);
                }

                @DisplayName("Then - 해당 레시피 재료들이 조회된다")
                @Test
                void thenIngredientsAreFound() {
                    assertThat(foundIngredients).hasSize(2);
                    foundIngredients.forEach(ingredient -> {
                        assertThat(ingredient.getRecipeId()).isEqualTo(recipeId);

                        assertThat(ingredient.getName()).isIn("재료1", "재료2");
                        assertThat(ingredient.getAmount()).isIn(100, 200);
                        assertThat(ingredient.getUnit()).isIn("g", "ml");
                        assertThat(ingredient.getCreatedAt()).isEqualTo(FIXED_TIME);
                    });
                }
            }
        }
    }
}
