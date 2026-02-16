package com.cheftory.api.recipe.content.verify.client;

import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientRequest;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface RecipeVerifyHttpApi {

    @PostExchange("/verify")
    RecipeVerifyClientResponse verify(@RequestBody RecipeVerifyClientRequest request);

    @DeleteExchange("/cleanup")
    void cleanupVideo(@RequestParam("file_uri") String fileUri);
}
