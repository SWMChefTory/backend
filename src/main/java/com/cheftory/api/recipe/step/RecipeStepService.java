package com.cheftory.api.recipe.step;

import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.helper.RecipeFinder;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepResponse;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.step.helper.RecipeStepCreator;
import com.cheftory.api.recipe.step.client.RecipeStepClient;
import com.cheftory.api.recipe.step.helper.RecipeStepFinder;
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
    private final RecipeFinder recipeFinder;
    private final RecipeStepCreator recipeStepCreator;
    private final RecipeStepFinder recipeStepFinder;

    @Transactional
    public List<UUID> create(UUID recipeId, CaptionInfo captionInfo, List<Ingredient> ingredients) {
        String videoId = recipeFinder.findVideoId(recipeId);
        ClientRecipeStepsResponse clientRecipeStepsResponse = recipeStepClient
                .fetchRecipeSteps(videoId, captionInfo, ingredients);

        List<ClientRecipeStepResponse> responses = clientRecipeStepsResponse
                .getSummary()
                .getSteps();

        List<RecipeStep> recipeSteps = IntStream.range(0, responses.size())
                .mapToObj(i ->
                        RecipeStep.from(i + 1, responses.get(i), recipeId))
                .toList();

        return recipeStepCreator.createAll(recipeSteps);
    }

    public List<RecipeStepInfo> getRecipeStepInfos(UUID recipeId) {
        return recipeStepFinder
                .findRecipeSteps(recipeId)
                .stream()
                .map(RecipeStepInfo::from)
                .toList();
    }
}
