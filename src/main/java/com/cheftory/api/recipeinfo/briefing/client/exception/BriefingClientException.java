package com.cheftory.api.recipeinfo.briefing.client.exception;

import com.cheftory.api.exception.CheftoryException;

public class BriefingClientException extends CheftoryException {

  public BriefingClientException(BriefingClientErrorCode e) {
    super(e);
  }
}
