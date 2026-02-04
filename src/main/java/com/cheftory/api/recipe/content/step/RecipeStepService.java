package com.cheftory.api.recipe.content.step;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.step.client.RecipeStepClient;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.step.entity.RecipeStepSort;
import com.cheftory.api.recipe.content.step.repository.RecipeStepRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeStepService {
    private final RecipeStepClient recipeStepClient;
    private final RecipeStepRepository recipeStepRepository;
    private final Clock clock;

    @DbThrottled
    public List<UUID> create(UUID recipeId, String fileUri, String mimeType) {
        ClientRecipeStepsResponse response = recipeStepClient.fetchRecipeSteps(fileUri, mimeType);

        List<RecipeStep> recipeSteps = response.toRecipeSteps(recipeId, clock);

        return recipeStepRepository.saveAll(recipeSteps).stream()
                .map(RecipeStep::getId)
                .toList();
    }

    public List<RecipeStep> gets(UUID recipeId) {
        return recipeStepRepository.findAllByRecipeId(recipeId, RecipeStepSort.STEP_ORDER_ASC);
    }
}
