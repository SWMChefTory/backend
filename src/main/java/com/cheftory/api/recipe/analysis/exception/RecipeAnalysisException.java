package com.cheftory.api.recipe.analysis.exception;

import com.cheftory.api.exception.CheftoryException;

public class RecipeAnalysisException extends CheftoryException {
  public RecipeAnalysisException(RecipeAnalysisErrorCode errorCode) {
    super(errorCode);
  }
}
