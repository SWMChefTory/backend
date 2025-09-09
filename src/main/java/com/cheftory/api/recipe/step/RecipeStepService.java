package com.cheftory.api.recipe.step;

import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.step.client.RecipeStepClient;
import com.cheftory.api.recipe.step.entity.RecipeStepSort;
import com.cheftory.api.recipe.step.repository.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeStepService {
    private final RecipeStepClient recipeStepClient;
    private final RecipeStepRepository recipeStepRepository;

    @Transactional
    public List<UUID> create(String videoId, UUID recipeId, RecipeCaption recipeCaption, List<RecipeAnalysis.Ingredient> ingredients) {
        ClientRecipeStepsResponse response = recipeStepClient
                .fetchRecipeSteps(videoId, recipeCaption, ingredients);

        List<RecipeStep> recipeSteps = response.toRecipeSteps(recipeId);

        return recipeStepRepository.saveAll(recipeSteps).stream()
            .map(RecipeStep::getId)
            .toList();
    }

    public List<RecipeStep> findByRecipeId(UUID recipeId) {
        return recipeStepRepository
                .findAllByRecipeId(recipeId, RecipeStepSort.STEP_ORDER_ASC);
    }
}
