package com.cheftory.api.recipe.content.verify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecipeVerifyClientResponse(
        @JsonProperty("file_uri") String fileUri, @JsonProperty("mime_type") String mimeType) {}
