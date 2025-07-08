package com.cheftory.api.recipe.ingredients.client;

import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.cheftory.api.recipe.ingredients.dto.ClientIngredientsResponse;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class RecipeIngredientsClient {
    public RecipeIngredientsClient(@Qualifier("recipeCreateClient") WebClient webClient){
        this.webClient = webClient;
    }
    private final WebClient webClient;

    public List<Ingredient> fetchRecipeIngredients(String videoId, CaptionInfo captionInfo) {
        ClientIngredientsResponse response = webClient.post().uri(uriBuilder -> uriBuilder
                        .path("/ingredients")
                        .queryParam("videoId", videoId)
                        .build())
                .bodyValue(captionInfo)
                .retrieve()
                .bodyToMono(ClientIngredientsResponse.class)
                .block();

        return response
                .getIngredients();
    }
}
