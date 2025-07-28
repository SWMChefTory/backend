package com.cheftory.api.recipe.caption;

import com.cheftory.api.recipe.caption.client.CaptionClient;
import com.cheftory.api.recipe.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.exception.CaptionErrorCode;
import com.cheftory.api.recipe.caption.exception.RecipeCaptionException;
import com.cheftory.api.recipe.caption.repository.RecipeCaptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeCaptionService {

  private final CaptionClient captionClient;
  private final RecipeCaptionRepository recipeCaptionRepository;

  @Transactional
  public UUID create(String videoId, UUID recipeId) {
    ClientCaptionResponse clientCaptionResponse = captionClient
        .fetchCaption(videoId);

    RecipeCaption recipeCaption = RecipeCaption.from(
        clientCaptionResponse.getCaptions()
        , clientCaptionResponse.getLangCodeType()
        , recipeId
    );

    return recipeCaptionRepository.save(recipeCaption).getId();
  }

  public RecipeCaption findByRecipeId(UUID recipeId) {
    return recipeCaptionRepository
        .findByRecipeId((recipeId))
        .orElseThrow(() ->
            new RecipeCaptionException(CaptionErrorCode.CAPTION_NOT_FOUND)
        );
  }

  public RecipeCaption find(UUID captionId) {
    return recipeCaptionRepository
        .findById((captionId))
        .orElseThrow(() ->
            new RecipeCaptionException(CaptionErrorCode.CAPTION_NOT_FOUND)
        );
  }
}
