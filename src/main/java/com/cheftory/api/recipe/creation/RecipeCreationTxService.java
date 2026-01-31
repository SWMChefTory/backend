package com.cheftory.api.recipe.creation;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import jakarta.transaction.Transactional;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCreationTxService {
    private final RecipeInfoService recipeInfoService;
    private final RecipeIdentifyService recipeIdentifyService;

    @Transactional
    public RecipeInfo createWithIdentify(URI url) {
        RecipeInfo recipeInfo = recipeInfoService.create();
        recipeIdentifyService.create(url, recipeInfo.getId());
        return recipeInfo;
    }
}
