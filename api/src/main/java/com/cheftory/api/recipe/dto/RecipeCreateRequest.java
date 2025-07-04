package com.cheftory.api.recipe.dto;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class RecipeCreateRequest {
    private String videoUrl;
    public UriComponents getVideoUrl() {
        return UriComponentsBuilder
                .fromUriString(videoUrl)
                .build();
    }
}
