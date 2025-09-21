package com.cheftory.api.recipeinfo.progress;

import com.cheftory.api.recipeinfo.recipe.validator.ExistsRecipeId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/progress")
public class RecipeProgressController {
  private final RecipeProgressService recipeProgressService;

  @GetMapping("/{recipeId}")
  public RecipeProgressResponse getRecipeProgress(
      @PathVariable("recipeId") @ExistsRecipeId UUID recipeId) {
    List<RecipeProgress> recipeProgresses = recipeProgressService.finds(recipeId);
    return RecipeProgressResponse.of(recipeProgresses);
  }
}
