package com.cheftory.api.recipe.content.step.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClientRecipeStepsRequest(
        @JsonProperty("file_uri") String fileUri,
        @JsonProperty("mime_type") String mimeType) {

    public static ClientRecipeStepsRequest from(String fileUri, String mimeType) {
        return new ClientRecipeStepsRequest(fileUri, mimeType);
    }
}
