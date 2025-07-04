package com.cheftory.api.recipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
public class RecipeCreateRequest {
    private String videoUrl;
    @Schema(hidden = true)
    public UriComponents getVideoUriComponents() {
        return UriComponentsBuilder
                .fromUriString(videoUrl)
                .build();
    }
}
