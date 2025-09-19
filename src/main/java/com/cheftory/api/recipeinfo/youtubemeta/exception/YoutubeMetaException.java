package com.cheftory.api.recipeinfo.youtubemeta.exception;

import com.cheftory.api.exception.ErrorMessage;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class YoutubeMetaException extends RecipeInfoException {

  public YoutubeMetaException(ErrorMessage errorMessage) {
    super(errorMessage);
  }
}
