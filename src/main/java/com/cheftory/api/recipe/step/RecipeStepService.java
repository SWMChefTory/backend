package com.cheftory.api.recipe.step;

import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepResponse;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.step.client.RecipeStepClient;
import com.cheftory.api.recipe.step.repository.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RecipeStepService {
    private final RecipeStepClient recipeStepClient;
    private final RecipeStepRepository recipeStepRepository;

    @Transactional
    public List<UUID> create(String videoId, UUID recipeId, CaptionInfo captionInfo, List<Ingredient> ingredients) {
        ClientRecipeStepsResponse clientRecipeStepsResponse = recipeStepClient
                .fetchRecipeSteps(videoId, captionInfo, ingredients);

        List<ClientRecipeStepResponse> responses = clientRecipeStepsResponse
                .getSummary()
                .getSteps();

        List<RecipeStep> recipeSteps = IntStream.range(0, responses.size())
                .mapToObj(i ->
                        RecipeStep.from(i + 1, responses.get(i), recipeId))
                .toList();

        return recipeStepRepository.saveAll(recipeSteps).stream()
            .map(RecipeStep::getId)
            .toList();
    }

    public List<RecipeStepInfo> getRecipeStepInfos(UUID recipeId) {
        return recipeStepRepository
                .findAllByRecipeId(recipeId)
                .stream()
                .map(RecipeStepInfo::from)
                .toList();
    }
}
