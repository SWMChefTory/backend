package com.cheftory.api.exception;

import lombok.Getter;

@Getter
public class CheftoryException extends Exception {
    private final Error error;

    public CheftoryException(Error error) {
        this.error = error;
    }
}
