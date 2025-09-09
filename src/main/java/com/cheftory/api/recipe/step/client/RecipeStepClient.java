package com.cheftory.api.recipe.step.client;

import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepsRequest;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class RecipeStepClient {
    public RecipeStepClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    private final WebClient webClient;

    public ClientRecipeStepsResponse fetchRecipeSteps(String videoId, RecipeCaption recipeCaption, List<RecipeAnalysis.Ingredient> ingredients) {
        ClientRecipeStepsRequest request = ClientRecipeStepsRequest
                .from(videoId, "youtube", recipeCaption, ingredients);
      return webClient.post()
              .uri(uriBuilder -> uriBuilder
                      .path("/steps")
                      .build())
              .bodyValue(request)
              .retrieve()
              .bodyToMono(ClientRecipeStepsResponse.class)
              .block();
    }
}
