package com.cheftory.api.recipe.ingredients.client;

import com.cheftory.api.recipe.ingredients.client.dto.ClientIngredientsResponse;
import org.apache.catalina.authenticator.SavedRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RecipeIngredientsClient {
    public RecipeIngredientsClient(@Qualifier("recipeCreateClient") WebClient webClient){
        this.webClient = webClient;
    }
    private final WebClient webClient;

    public String fetchRecipeIngredients(String videoId, String captionContent) {
        ClientIngredientsResponse response = webClient.post().uri(uriBuilder -> uriBuilder
                .path("/ingredients")
                .queryParam("videoId", videoId)
                .build())
                .bodyValue(captionContent)
                .retrieve()
                .bodyToMono(ClientIngredientsResponse.class)
                .block();

        return response
                .toString();
    }
}
