package com.cheftory.api.recipe.caption.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ClientCaptionResponse {
    @JsonProperty("lang_code")
    private String langCode;
    private List<Segment> segments;
}
