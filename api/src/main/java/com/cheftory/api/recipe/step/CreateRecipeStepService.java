package com.cheftory.api.recipe.step;

import com.cheftory.api.recipe.step.client.RecipeStepClient;
import com.cheftory.api.recipe.step.dto.ClientStepResponse;
import com.cheftory.api.recipe.step.dto.ClientStepsResponse;
import com.cheftory.api.recipe.step.dto.StepCreateInfo;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.step.repository.RecipeStepRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@NoArgsConstructor
public class CreateRecipeStepService {
    private RecipeStepClient recipeStepClient;
    private RecipeStepRepository recipeStepRepository;
    public List<UUID> createAll(String videoId,UUID recipeId,String segments) {
        List<ClientStepResponse> clientStepResponses = recipeStepClient
                .fetchRecipeSteps(videoId,segments)
                .getSteps();

        List<RecipeStep> recipeSteps = generateRecipeSteps(clientStepResponses,recipeId);

        return recipeStepRepository.saveAll(recipeSteps)
                .stream()
                .map(RecipeStep::getRecipeInfoId)
                .toList();
    }

    public List<RecipeStep> generateRecipeSteps(List<ClientStepResponse> clientStepResponses, UUID recipeId) {
        List<RecipeStep> recipeSteps = new ArrayList<>();
        Integer order= 1;
        for(ClientStepResponse clientStepResponse: clientStepResponses){
            recipeSteps.add(RecipeStep.from(
                    clientStepResponse.getSubtitle()
                    ,order
                    ,clientStepResponse.getDetails().toString()
                    ,clientStepResponse.getStart()
                    ,clientStepResponse.getEnd()
                    ,recipeId
            ));
            order++;
        }
        return recipeSteps;
    }
}
