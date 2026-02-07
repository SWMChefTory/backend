package com.cheftory.api._common.reponse;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.Error;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private String message;
    private String errorCode;

    public static ErrorResponse of(Error error) {
        return new ErrorResponse(error.getMessage(), error.getErrorCode());
    }

    public static ErrorResponse from(CheftoryException exception) {
        return new ErrorResponse(
                exception.getError().getMessage(), exception.getError().getErrorCode());
    }
}
