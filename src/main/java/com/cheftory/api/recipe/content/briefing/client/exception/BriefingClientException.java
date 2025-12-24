package com.cheftory.api.recipe.content.briefing.client.exception;

import com.cheftory.api.exception.CheftoryException;

public class BriefingClientException extends CheftoryException {

  public BriefingClientException(BriefingClientErrorCode e) {
    super(e);
  }
}
