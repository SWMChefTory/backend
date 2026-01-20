package com.cheftory.api._common.reponse;

import lombok.Getter;

public record SuccessOnlyResponse(@Getter String message) {
    public static SuccessOnlyResponse create() {
        return new SuccessOnlyResponse("success");
    }
}
