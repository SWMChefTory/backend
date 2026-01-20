package com.cheftory.api.recipe.content.step;

import com.cheftory.api.recipe.content.info.validator.ExistsRecipeId;
import com.cheftory.api.recipe.content.step.dto.RecipeStepsResponse;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/papi/v1/recipes")
@RequiredArgsConstructor
public class RecipeStepController {

    public final RecipeStepService recipeStepService;

    @GetMapping("/{recipeId}/steps")
    public RecipeStepsResponse getRecipeSteps(@PathVariable @ExistsRecipeId UUID recipeId) {
        List<RecipeStep> recipesStep = recipeStepService.gets(recipeId);
        return RecipeStepsResponse.from(recipesStep);
    }
}
