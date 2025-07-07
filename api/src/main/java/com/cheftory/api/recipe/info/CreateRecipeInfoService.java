package com.cheftory.api.recipe.info;

import com.cheftory.api.recipe.info.client.YoutubeUrlNormalizer;
import com.cheftory.api.recipe.info.entity.RecipeInfo;
import com.cheftory.api.recipe.info.repository.RecipeInfoRepository;
import com.cheftory.api.recipe.info.client.RecipeInfoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CreateRecipeInfoService {
    private final RecipeInfoClient recipeMetaClient;
    private final YoutubeUrlNormalizer youtubeUrlNormalizer;
    private final RecipeInfoRepository recipeMetaRepository;

    //url형식이기만 하면 요청을 받는다.
    public UUID create(UriComponents url) {
        UriComponents urlNormalized = youtubeUrlNormalizer.normalize(url);
        RecipeInfo info = recipeMetaClient.fetchRecipeInfo(urlNormalized);

        return recipeMetaRepository
                .save(info)
                .getId();
    }

}
