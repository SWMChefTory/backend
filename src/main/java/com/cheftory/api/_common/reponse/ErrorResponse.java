package com.cheftory.api._common.reponse;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ErrorResponse {

  private String message;
  private String errorCode;

  public static ErrorResponse of(ErrorMessage errorMessage) {
    return ErrorResponse.builder()
        .message(errorMessage.getMessage())
        .errorCode(errorMessage.getErrorCode())
        .build();
  }

  public static ErrorResponse from(CheftoryException exception) {
    return ErrorResponse.builder()
        .message(exception.getErrorMessage().getMessage())
        .errorCode(exception.getErrorMessage().getErrorCode())
        .build();
  }
}
