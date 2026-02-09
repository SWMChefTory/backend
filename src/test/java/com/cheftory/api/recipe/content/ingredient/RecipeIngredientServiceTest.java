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
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("재료 목록을 엔티티로 변환하여 저장한다")
        void it_converts_and_saves_ingredients() {
            // Given
            UUID recipeId = UUID.randomUUID();
            List<Ingredient> ingredients =
                    List.of(new Ingredient("Sugar", 100, "g"), new Ingredient("Flour", 200, "g"));
            doNothing().when(recipeIngredientRepository).create(anyList());

            // When
            recipeIngredientService.create(recipeId, ingredients);

            // Then
            ArgumentCaptor<List<RecipeIngredient>> captor = ArgumentCaptor.forClass(List.class);
            verify(recipeIngredientRepository).create(captor.capture());

            List<RecipeIngredient> saved = captor.getValue();
            assertThat(saved).hasSize(2);
            assertThat(saved).extracting(RecipeIngredient::getName).containsExactlyInAnyOrder("Sugar", "Flour");
        }
    }

    @Nested
    @DisplayName("gets 메서드는")
    class Describe_gets {

        @Test
        @DisplayName("레시피 ID로 재료 목록을 조회한다")
        void it_returns_ingredients_by_recipe_id() {
            // Given
            UUID recipeId = UUID.randomUUID();
            List<RecipeIngredient> expected = List.of(mock(RecipeIngredient.class));
            doReturn(expected).when(recipeIngredientRepository).finds(recipeId);

            // When
            List<RecipeIngredient> result = recipeIngredientService.gets(recipeId);

            // Then
            assertThat(result).isEqualTo(expected);
            verify(recipeIngredientRepository).finds(recipeId);
        }
    }
}
