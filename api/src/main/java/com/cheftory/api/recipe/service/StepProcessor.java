package com.cheftory.api.recipe.service;

import com.cheftory.api.recipe.info.UpdateRecipeInfoService;
import com.cheftory.api.recipe.info.entity.RecipeStatus;
import com.cheftory.api.recipe.step.CreateRecipeStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StepProcessor {
    private final UpdateRecipeInfoService updateRecipeInfoService;
    private final CreateRecipeStepService createRecipeStepService;

    @Transactional
    public List<UUID> process(UUID recipeId, String videoId) {
        updateRecipeInfoService.updateState(recipeId, RecipeStatus.CREATING_STEPS);
        return createRecipeStepService.createAll(videoId,recipeId);
    }
}
