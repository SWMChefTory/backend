package com.cheftory.api.exception;

public class CheftoryException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public CheftoryException(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
