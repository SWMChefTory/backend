package com.example.mockrecipecreator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@ToString
public class ClientCaptionResponse {
    @JsonProperty("lang_code")
    private String langCode;
    private List<Segment> segments;
}
