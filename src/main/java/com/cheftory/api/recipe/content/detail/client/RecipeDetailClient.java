package com.cheftory.api.recipe.content.detail.client;

import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailRequest;
import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class RecipeDetailClient {
    public RecipeDetailClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    private final WebClient webClient;

    public ClientRecipeDetailResponse fetchRecipeDetails(String videoId, String fileUri, String mimeType) {
        ClientRecipeDetailRequest request = ClientRecipeDetailRequest.from(videoId, fileUri, mimeType);

        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/meta/video").build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClientRecipeDetailResponse.class)
                .block();
    }
}
