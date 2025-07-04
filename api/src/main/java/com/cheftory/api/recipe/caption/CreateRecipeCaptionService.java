package com.cheftory.api.recipe.caption;

import com.cheftory.api.recipe.caption.client.CaptionClient;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.repository.RecipeCaptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRecipeCaptionService {
    private final RecipeCaptionRepository recipeCaptionRepository;
    private final CaptionClient captionClient;

    public UUID create(String videoId, UUID recipeId) {
        String clientCaptionResponse = captionClient.fetchCaption(videoId);
        RecipeCaption recipeCaption = RecipeCaption.from(
                clientCaptionResponse
                ,recipeId
        );
        return recipeCaptionRepository
                .save(recipeCaption)
                .getId();
    }
}
