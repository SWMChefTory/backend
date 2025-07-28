package com.cheftory.api.recipe.step;

import com.cheftory.api.recipe.step.dto.RecipeStepsResponse;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/papi/v1/recipes")
@RequiredArgsConstructor
public class RecipeStepController {

  public final RecipeStepService recipeStepService;

  @GetMapping("/{recipeId}/steps")
  public RecipeStepsResponse getRecipeSteps(@PathVariable UUID recipeId) {
    List<RecipeStep> recipesStep = recipeStepService
        .findByRecipeId(recipeId);
    return RecipeStepsResponse.from(recipesStep);
  }
}
