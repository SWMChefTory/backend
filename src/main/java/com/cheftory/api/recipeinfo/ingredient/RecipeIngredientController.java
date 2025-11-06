package com.cheftory.api.recipeinfo.ingredient;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/papi/v1/recipes")
public class RecipeIngredientController {

  private final RecipeIngredientService recipeIngredientService;

  @GetMapping("/ingredients/{recipeId}")
  public RecipeIngredientsResponse getIngredients(@PathVariable UUID recipeId) {
    List<RecipeIngredient> recipeIngredients = recipeIngredientService.gets(recipeId);
    return RecipeIngredientsResponse.from(recipeIngredients);
  }
}
