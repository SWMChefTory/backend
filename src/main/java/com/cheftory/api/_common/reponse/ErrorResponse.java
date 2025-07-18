package com.cheftory.api._common.reponse;

import com.cheftory.api.exception.CheftoryException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ErrorResponse {

  private String message;
  private String error;

  public static ErrorResponse of(String message,String error) {
    return ErrorResponse.builder()
        .message(message)
        .error(error)
        .build();
  }

  public static ErrorResponse from(CheftoryException exception) {
    return ErrorResponse.builder()
        .message(exception.getMessage())
        .error(exception.getError())
        .build();
  }
}
