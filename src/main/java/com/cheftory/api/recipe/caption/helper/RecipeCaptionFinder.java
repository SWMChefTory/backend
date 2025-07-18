package com.cheftory.api.recipe.caption.helper;

import com.cheftory.api.recipe.caption.CaptionNotFoundException;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.error_code.CaptionErrorCode;
import com.cheftory.api.recipe.caption.helper.repository.RecipeCaptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeCaptionFinder {

  private final RecipeCaptionRepository recipeCaptionRepository;

  public RecipeCaption findById(UUID recipeCaptionId) {
    return recipeCaptionRepository
        .findById(recipeCaptionId)
        .orElseThrow(() -> new CaptionNotFoundException(CaptionErrorCode.CAPTION_NOT_FOUND));
  }
}
