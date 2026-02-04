package com.cheftory.api.recipe.content.detail.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClientRecipeDetailRequest(
        @JsonProperty("video_id") String videoId,
        @JsonProperty("file_uri") String fileUri,
        @JsonProperty("mime_type") String mimeType) {

    public static ClientRecipeDetailRequest from(String videoId, String fileUri, String mimeType) {
        return new ClientRecipeDetailRequest(videoId, fileUri, mimeType);
    }
}
