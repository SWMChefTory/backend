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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
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
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("여러 재료 정보를 한 번에 저장한다")
        void it_saves_multiple_ingredients() {
            // Given
            UUID recipeId = UUID.randomUUID();
            RecipeIngredient ingredient1 = RecipeIngredient.create("재료1", "g", 100, recipeId, clock);
            RecipeIngredient ingredient2 = RecipeIngredient.create("재료2", "ml", 200, recipeId, clock);

            // When
            recipeIngredientRepository.create(List.of(ingredient1, ingredient2));

            // Then
            List<RecipeIngredient> results = recipeIngredientRepository.finds(recipeId);
            assertThat(results).hasSize(2);
            assertThat(results).extracting(RecipeIngredient::getName).containsExactlyInAnyOrder("재료1", "재료2");
        }
    }

    @Nested
    @DisplayName("findAllByRecipeId 메서드는")
    class Describe_findAllByRecipeId {

        @Test
        @DisplayName("특정 레시피의 모든 재료를 조회한다")
        void it_returns_all_ingredients_for_recipe() {
            // Given
            UUID recipeId = UUID.randomUUID();
            RecipeIngredient ingredient = RecipeIngredient.create("재료", "g", 100, recipeId, clock);
            recipeIngredientRepository.create(List.of(ingredient));

            // When
            List<RecipeIngredient> results = recipeIngredientRepository.finds(recipeId);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getRecipeId()).isEqualTo(recipeId);
        }
    }
}
