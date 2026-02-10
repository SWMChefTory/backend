package com.cheftory.api.recipe.content.ingredient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail.Ingredient;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.ingredient.repository.RecipeIngredientRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("RecipeIngredientService 테스트")
class RecipeIngredientServiceTest {

    private RecipeIngredientService recipeIngredientService;
    private RecipeIngredientRepository recipeIngredientRepository;
    private Clock clock;

    @BeforeEach
    void setUp() {
        recipeIngredientRepository = mock(RecipeIngredientRepository.class);
        clock = mock(Clock.class);
        recipeIngredientService = new RecipeIngredientService(recipeIngredientRepository, clock);
    }

    @Nested
    @DisplayName("레시피 재료 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 재료 목록이 주어졌을 때")
        class GivenIngredients {
            UUID recipeId;
            List<Ingredient> ingredients;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                ingredients = List.of(new Ingredient("Sugar", 100, "g"), new Ingredient("Flour", 200, "g"));
                doNothing().when(recipeIngredientRepository).create(anyList());
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() {
                    recipeIngredientService.create(recipeId, ingredients);
                }

                @Test
                @DisplayName("Then - 엔티티로 변환하여 저장한다")
                void thenConvertsAndSaves() {
                    ArgumentCaptor<List<RecipeIngredient>> captor = ArgumentCaptor.forClass(List.class);
                    verify(recipeIngredientRepository).create(captor.capture());

                    List<RecipeIngredient> saved = captor.getValue();
                    assertThat(saved).hasSize(2);
                    assertThat(saved).extracting(RecipeIngredient::getName).containsExactlyInAnyOrder("Sugar", "Flour");
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 재료 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 레시피 ID가 주어졌을 때")
        class GivenRecipeId {
            UUID recipeId;
            List<RecipeIngredient> expected;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                expected = List.of(mock(RecipeIngredient.class));
                doReturn(expected).when(recipeIngredientRepository).finds(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeIngredient> result;

                @BeforeEach
                void setUp() {
                    result = recipeIngredientService.gets(recipeId);
                }

                @Test
                @DisplayName("Then - 재료 목록을 반환한다")
                void thenReturnsIngredients() {
                    assertThat(result).isEqualTo(expected);
                    verify(recipeIngredientRepository).finds(recipeId);
                }
            }
        }
    }
}
