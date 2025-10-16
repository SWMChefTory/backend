package com.cheftory.api.recipeinfo.ingredient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.detail.RecipeDetail.Ingredient;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("RecipeIngredientServiceTest")
public class RecipeIngredientServiceTest {

  private RecipeIngredientService recipeIngredientService;
  private RecipeIngredientRepository recipeIngredientRepository;
  private Clock clock;

  @BeforeEach
  void setUp() {
    recipeIngredientRepository = mock(RecipeIngredientRepository.class);
    clock = mock(Clock.class);
    recipeIngredientService = new RecipeIngredientService(recipeIngredientRepository, clock);
  }

  @DisplayName("레시피 재료 생성")
  @Nested
  class CreateRecipeIngredient {

    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    @Nested
    class GivenValidParameters {

      private List<Ingredient> ingredients;
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        ingredients =
            List.of(new Ingredient("Sugar", 100, "grams"), new Ingredient("Flour", 200, "grams"));
        recipeId = UUID.randomUUID();
      }

      @DisplayName("When - 레시피 재료를 생성하면")
      @Nested
      class WhenCreateRecipeIngredient {

        @BeforeEach
        void setUp() {
          recipeIngredientService.create(recipeId, ingredients);
        }

        @DisplayName("Then - 레시피 재료가 생성된다")
        @Test
        void thenRecipeIngredientIsCreated() {

          @SuppressWarnings("unchecked")
          ArgumentCaptor<Iterable<RecipeIngredient>> captor =
              ArgumentCaptor.forClass(Iterable.class);

          verify(recipeIngredientRepository).saveAll(captor.capture());

          List<RecipeIngredient> recipeIngredients =
              StreamSupport.stream(captor.getValue().spliterator(), false).toList();

          assertThat(recipeIngredients).hasSize(2);

          recipeIngredients.forEach(ri -> assertThat(ri.getRecipeId()).isEqualTo(recipeId));

          assertThat(recipeIngredients)
              .extracting(RecipeIngredient::getName)
              .containsExactlyInAnyOrder("Sugar", "Flour");
          assertThat(recipeIngredients)
              .extracting(RecipeIngredient::getAmount)
              .containsExactlyInAnyOrder(100, 200);
          assertThat(recipeIngredients)
              .extracting(RecipeIngredient::getUnit)
              .containsExactlyInAnyOrder("grams", "grams");
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 ID로 재료 조회")
  class FindByRecipeId {

    @Nested
    @DisplayName("Given - 존재하는 레시피 ID가 주어졌을 때")
    class GivenExistingRecipeId {

      UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 ID로 재료를 조회하면")
      class WhenFindByRecipeId {
        List<RecipeIngredient> expectedIngredients;

        @BeforeEach
        void setUp() {
          expectedIngredients = List.of(mock(RecipeIngredient.class), mock(RecipeIngredient.class));
          doReturn(expectedIngredients)
              .when(recipeIngredientRepository)
              .findAllByRecipeId(recipeId);
        }

        @Test
        @DisplayName("Then - 해당 재료들이 반환된다")
        void thenIngredientsAreReturned() {
          List<RecipeIngredient> result = recipeIngredientService.gets(recipeId);
          assertThat(result).isEqualTo(expectedIngredients);
          verify(recipeIngredientRepository).findAllByRecipeId(recipeId);
        }
      }
    }
  }
}
