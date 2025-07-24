package com.cheftory.api.recipe.ingredients.client;

import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.ingredients.client.dto.ClientIngredientsRequest;
import com.cheftory.api.recipe.ingredients.client.dto.ClientIngredientsResponse;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class RecipeIngredientsClient {
    public RecipeIngredientsClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    private final WebClient webClient;

    public ClientIngredientsResponse fetchRecipeIngredients(String videoId, CaptionInfo captionInfo) {
        ClientIngredientsRequest request = ClientIngredientsRequest.from(videoId, "youtube", captionInfo);

        return webClient.post().uri(uriBuilder -> uriBuilder
                        .path("/ingredients")
                        .build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClientIngredientsResponse.class)
                .block();
    }
}
