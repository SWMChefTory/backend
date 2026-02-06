package com.cheftory.api.exception;

public class CheftoryException extends RuntimeException {
    private final Error error;

    public CheftoryException(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
