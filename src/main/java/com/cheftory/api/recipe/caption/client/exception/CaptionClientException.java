package com.cheftory.api.recipe.caption.client.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;

public class CaptionClientException extends CheftoryException {

  public CaptionClientException(CaptionClientErrorCode e) {
    super(e);
  }
}
