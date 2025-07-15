package com.cheftory.api.recipe.step.helper;

import com.cheftory.api.recipe.helper.repository.RecipeNotFoundException;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.step.helper.repository.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeStepFinder {
    private final RecipeStepRepository recipeStepRepository;

    public List<RecipeStep> findRecipeSteps(UUID recipeId) {
        List<RecipeStep> recipeSteps = recipeStepRepository
                .findAllByRecipeId(recipeId);
        if (recipeSteps.isEmpty()) {
            throw new RecipeNotFoundException("레시피 단계들이 존재하지 않습니다.");
        }
        return recipeSteps;
    }
}
