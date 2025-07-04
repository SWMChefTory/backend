package com.cheftory.api.recipe.step.client;

import com.cheftory.api.recipe.step.dto.ClientStepsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RecipeStepClient {
    public RecipeStepClient(@Qualifier("recipeCreateClient") WebClient webClient){
        this.webClient = webClient;
    }
    private final WebClient webClient;

    public ClientStepsResponse fetchRecipeSteps(String videoId){
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/step")
                        .queryParam("videoId", videoId)
                        .build())
                .retrieve()
                .bodyToMono(ClientStepsResponse.class)
                .block();
    }
}
