package com.cheftory.api.recipe.caption.client.dto;

import com.cheftory.api.recipe.caption.entity.LangCodeType;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ClientCaptionResponse {
    @JsonProperty("lang_code")
    private LangCodeType langCodeType;
    private List<Segment> segments;
}
