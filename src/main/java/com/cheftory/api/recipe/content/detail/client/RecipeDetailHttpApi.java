package com.cheftory.api.recipe.content.detail.client;

import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailRequest;
import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface RecipeDetailHttpApi {

    @PostExchange("/meta/video")
    ClientRecipeDetailResponse fetch(@RequestBody ClientRecipeDetailRequest request);
}
