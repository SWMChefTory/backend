package com.cheftory.api.recipeinfo.caption.client.exception;

import com.cheftory.api.exception.CheftoryException;

public class CaptionClientException extends CheftoryException {

  public CaptionClientException(CaptionClientErrorCode e) {
    super(e);
  }
}
