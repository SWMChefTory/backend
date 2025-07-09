package com.cheftory.api.recipe.caption;

import com.cheftory.api.recipe.caption.client.CaptionClient;
import com.cheftory.api.recipe.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.caption.entity.LangCodeType;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.cheftory.api.recipe.caption.helper.RecipeCaptionCreator;
import com.cheftory.api.recipe.caption.helper.RecipeCaptionFinder;
import com.cheftory.api.recipe.helper.RecipeFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeCaptionService {
    private final RecipeFinder recipeFinder;
    private final CaptionClient captionClient;
    private final RecipeCaptionCreator recipeCaptionCreator;
    private final RecipeCaptionFinder recipeCaptionFinder;

    @Transactional
    public UUID create(UUID recipeId) {
        String videoId = recipeFinder.findVideoId(recipeId);

        ClientCaptionResponse clientCaptionResponse = captionClient
                .fetchCaption(videoId);

        RecipeCaption recipeCaption = RecipeCaption.from(
                clientCaptionResponse.getCaptions()
                , clientCaptionResponse.getLangCodeType()
                , recipeId
        );

        return recipeCaptionCreator.create(recipeCaption);
    }

    public CaptionInfo getCaptionInfo(UUID captionId) {
        RecipeCaption recipeCaption = recipeCaptionFinder.findById(captionId);
        return CaptionInfo.from(recipeCaption.getLangCode(), recipeCaption.getSegments());
    }

}
