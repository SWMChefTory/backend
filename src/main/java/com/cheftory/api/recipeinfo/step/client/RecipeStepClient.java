package com.cheftory.api.recipeinfo.step.client;

import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.step.client.dto.ClientRecipeStepsRequest;
import com.cheftory.api.recipeinfo.step.client.dto.ClientRecipeStepsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RecipeStepClient {
  public RecipeStepClient(@Qualifier("recipeCreateClient") WebClient webClient) {
    this.webClient = webClient;
  }

  private final WebClient webClient;

  public ClientRecipeStepsResponse fetchRecipeSteps(RecipeCaption recipeCaption) {
    ClientRecipeStepsRequest request = ClientRecipeStepsRequest.from(recipeCaption);
    return webClient
        .post()
        .uri(uriBuilder -> uriBuilder.path("/steps").build())
        .bodyValue(request)
        .retrieve()
        .bodyToMono(ClientRecipeStepsResponse.class)
        .block();
  }
}
