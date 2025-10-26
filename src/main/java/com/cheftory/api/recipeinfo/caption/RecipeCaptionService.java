package com.cheftory.api.recipeinfo.caption;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.caption.client.CaptionClient;
import com.cheftory.api.recipeinfo.caption.client.exception.CaptionClientErrorCode;
import com.cheftory.api.recipeinfo.caption.client.exception.CaptionClientException;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.caption.exception.RecipeCaptionErrorCode;
import com.cheftory.api.recipeinfo.caption.exception.RecipeCaptionException;
import com.cheftory.api.recipeinfo.caption.repository.RecipeCaptionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCaptionService {

  private final CaptionClient captionClient;
  private final RecipeCaptionRepository recipeCaptionRepository;
  private final Clock clock;

  public UUID create(String videoId, UUID recipeId) {
    try {
      RecipeCaption recipeCaption =
          captionClient.fetchCaption(videoId).toRecipeCaption(recipeId, clock);
      return recipeCaptionRepository.save(recipeCaption).getId();
    } catch (CaptionClientException e) {
      if (e.getErrorMessage() == CaptionClientErrorCode.NOT_COOK_VIDEO) {
        throw new RecipeCaptionException(RecipeCaptionErrorCode.NOT_COOK_RECIPE);
      }
      throw new RecipeCaptionException(RecipeCaptionErrorCode.CAPTION_CREATE_FAIL);
    }
  }

  public RecipeCaption findByRecipeId(UUID recipeId) {
    return recipeCaptionRepository
        .findByRecipeId((recipeId))
        .orElseThrow(() -> new RecipeCaptionException(RecipeCaptionErrorCode.CAPTION_NOT_FOUND));
  }

  public RecipeCaption get(UUID captionId) {
    return recipeCaptionRepository
        .findById((captionId))
        .orElseThrow(() -> new RecipeCaptionException(RecipeCaptionErrorCode.CAPTION_NOT_FOUND));
  }
}
