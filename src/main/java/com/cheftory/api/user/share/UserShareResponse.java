package com.cheftory.api.user.share;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserShareResponse(@JsonProperty("share_count") int shareCount) {

    public static UserShareResponse of(int shareCount) {
        return new UserShareResponse(shareCount);
    }
}
