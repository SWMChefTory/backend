package com.cheftory.api.recipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Getter
@Slf4j
public class RecipeCreateRequest {
    private URI videoUrl;
    @Schema(hidden = true)
    public UriComponents getVideoUriComponents() {
        log.trace(videoUrl.toString());
        return UriComponentsBuilder
                .fromUri(videoUrl)
                .build();
    }
}
