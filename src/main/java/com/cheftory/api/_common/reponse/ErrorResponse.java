package com.cheftory.api._common.reponse;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private String message;
    private String errorCode;

    public static ErrorResponse of(ErrorMessage errorMessage) {
        return new ErrorResponse(errorMessage.getMessage(), errorMessage.getErrorCode());
    }

    public static ErrorResponse from(CheftoryException exception) {
        return new ErrorResponse(
                exception.getErrorMessage().getMessage(),
                exception.getErrorMessage().getErrorCode());
    }
}
