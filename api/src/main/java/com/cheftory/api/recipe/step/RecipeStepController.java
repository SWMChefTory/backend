package com.cheftory.api.recipe.step;

import com.cheftory.api.recipe.step.dto.RecipeStepFindResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/recipeinfo")
@RequiredArgsConstructor
public class RecipeStepController {
    private final FindRecipeStepService findRecipeStepService;

    @GetMapping("/{recipeInfoId}/step/")
    public List<RecipeStepFindResponse> findRecipeSteps(
            @PathVariable UUID recipeInfoId
    ) {
        return findRecipeStepService.findAllByRecipeInfoId(recipeInfoId);
    }
}
