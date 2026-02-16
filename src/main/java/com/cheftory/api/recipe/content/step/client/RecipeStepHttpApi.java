package com.cheftory.api.recipe.content.step.client;

import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsRequest;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface RecipeStepHttpApi {

    @PostExchange("/steps/video")
    ClientRecipeStepsResponse fetch(@RequestBody ClientRecipeStepsRequest request);
}
