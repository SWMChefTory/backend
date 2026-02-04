package com.cheftory.api.recipe.content.verify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeVerifyClientRequest {
    @JsonProperty("video_id")
    private String videoId;

    public static RecipeVerifyClientRequest from(String videoId) {
        return new RecipeVerifyClientRequest(videoId);
    }
}
