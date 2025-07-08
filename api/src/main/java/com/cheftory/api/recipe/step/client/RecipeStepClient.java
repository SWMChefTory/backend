package com.cheftory.api.recipe.step.client;

import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepRequest;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class RecipeStepClient {
    public RecipeStepClient(@Qualifier("recipeCreateClient") WebClient webClient){
        this.webClient = webClient;
    }
    private final WebClient webClient;

    public ClientRecipeStepsResponse fetchRecipeSteps(String videoId, CaptionInfo captionInfo, List<Ingredient> ingredients) {

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/step")
                        .queryParam("videoId", videoId)
                        .build())
                .bodyValue(ClientRecipeStepRequest.from(captionInfo, ingredients))
                .retrieve()
                .bodyToMono(ClientRecipeStepsResponse.class)
                .block();
    }
}
