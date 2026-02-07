package com.cheftory.api.recipe.creation;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCreationTxService {
    private final RecipeInfoService recipeInfoService;
    private final RecipeIdentifyService recipeIdentifyService;
    private final RecipeYoutubeMetaService recipeYoutubeMetaService;

    @Transactional
    public RecipeInfo createWithIdentifyWithVideoInfo(YoutubeVideoInfo videoInfo) {
        RecipeInfo recipeInfo = recipeInfoService.create();
        recipeIdentifyService.create(videoInfo.getVideoUri());
        recipeYoutubeMetaService.create(videoInfo, recipeInfo.getId());
        return recipeInfo;
    }
}
