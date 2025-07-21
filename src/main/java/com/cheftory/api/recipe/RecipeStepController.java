package com.cheftory.api.recipe;

import com.cheftory.api.recipe.step.RecipeStepService;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
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
  public List<RecipeStepInfo> getRecipeSteps(@PathVariable UUID recipeId) {
    return recipeStepService
        .getRecipeStepInfos(recipeId);
  }
}
