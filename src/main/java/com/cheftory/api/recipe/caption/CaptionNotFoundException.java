package com.cheftory.api.recipe.caption;

public class CaptionNotFoundException extends RuntimeException {
    public CaptionNotFoundException(String message) {
        super(message);
    }
}