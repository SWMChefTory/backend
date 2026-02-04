package com.cheftory.api.recipe.content.step.client;

import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsRequest;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RecipeStepClient {
    public RecipeStepClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    private final WebClient webClient;

    public ClientRecipeStepsResponse fetchRecipeSteps(String fileUri, String mimeType) {
        ClientRecipeStepsRequest request = ClientRecipeStepsRequest.from(fileUri, mimeType);
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/steps/video").build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClientRecipeStepsResponse.class)
                .block();
    }
}
