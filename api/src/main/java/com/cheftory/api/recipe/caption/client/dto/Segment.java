package com.cheftory.api.recipe.caption.client.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Segment {
    private Double start;
    private Double end;
    private String text;
}
