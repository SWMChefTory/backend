package com.cheftory.api.recipe.content.step.client;

import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsRequest;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.content.step.exception.RecipeStepErrorCode;
import com.cheftory.api.recipe.content.step.exception.RecipeStepException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 외부 레시피 단계 생성 API와 통신하는 클라이언트 구현체
 */
@Slf4j
@Component
public class RecipeStepExternalClient implements RecipeStepClient {
    private final WebClient webClient;

    /**
     * RecipeStepExternalClient 생성자
     *
     * @param webClient 레시피 생성 전용 WebClient
     */
    public RecipeStepExternalClient(@Qualifier("recipeCreateClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public ClientRecipeStepsResponse fetch(String fileUri, String mimeType) throws RecipeStepException {
        ClientRecipeStepsRequest request = ClientRecipeStepsRequest.from(fileUri, mimeType);
        try {
            return webClient
                    .post()
                    .uri(uriBuilder -> uriBuilder.path("/steps/video").build())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClientRecipeStepsResponse.class)
                    .block();
        } catch (Exception e) {
            log.warn("레시피 생성중 오류 발생", e);
            throw new RecipeStepException(RecipeStepErrorCode.RECIPE_STEP_CREATE_FAIL, e);
        }
    }
}
