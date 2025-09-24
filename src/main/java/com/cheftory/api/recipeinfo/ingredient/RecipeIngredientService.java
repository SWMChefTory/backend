package com.cheftory.api.recipeinfo.ingredient;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.detail.RecipeDetail.Ingredient;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeIngredientService {
  private final RecipeIngredientRepository recipeIngredientRepository;
  private final Clock clock;

  public void create(UUID recipeId, List<Ingredient> ingredients) {
    List<RecipeIngredient> recipeIngredients =
        ingredients.stream()
            .map(
                ingredient ->
                    RecipeIngredient.create(
                        ingredient.name(), ingredient.unit(), ingredient.amount(), recipeId, clock))
            .toList();
    recipeIngredientRepository.saveAll(recipeIngredients);
  }

  public List<RecipeIngredient> finds(UUID recipeId) {
    return recipeIngredientRepository.findAllByRecipeId(recipeId);
  }
}
