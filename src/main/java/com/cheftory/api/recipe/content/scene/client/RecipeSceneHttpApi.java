package com.cheftory.api.recipe.content.scene.client;

import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesRequest;
import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface RecipeSceneHttpApi {

    @PostExchange("/scenes/video")
    ClientRecipeScenesResponse fetch(@RequestBody ClientRecipeScenesRequest request);
}
