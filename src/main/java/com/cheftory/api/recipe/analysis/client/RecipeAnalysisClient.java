package com.cheftory.api.recipe.analysis.client;

import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.analysis.client.dto.ClientRecipeAnalysisRequest;
import com.cheftory.api.recipe.analysis.client.dto.ClientRecipeAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class RecipeAnalysisClient {
    public RecipeAnalysisClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    private final WebClient webClient;

    public ClientRecipeAnalysisResponse fetchRecipeIngredients(String videoId, RecipeCaption recipeCaption) {
        ClientRecipeAnalysisRequest request = ClientRecipeAnalysisRequest.from(videoId, recipeCaption);

        return webClient.post().uri(uriBuilder -> uriBuilder
                .path("/meta")
                .build())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ClientRecipeAnalysisResponse.class)
            .block();
    }
}