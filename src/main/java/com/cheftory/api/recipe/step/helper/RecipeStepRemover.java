package com.cheftory.api.recipe.step.helper;

import com.cheftory.api.recipe.step.helper.repository.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeStepRemover {
    private final RecipeStepRepository recipeStepRepository;

    public void removeAllByRecipeId(UUID recipeId) {
        recipeStepRepository.deleteAllByRecipeId(recipeId);
    }
}
