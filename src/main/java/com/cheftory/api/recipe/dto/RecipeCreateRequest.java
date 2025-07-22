package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public record RecipeCreateRequest(
    @JsonProperty("video_url")
    URI videoUrl
) {}