package com.example.mockrecipecreator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Segment {
    private Double start;
    private Double end;
    private String text;

    public Segment() {}

    public Segment(Double start, Double end, String text) {
        this.start = start;
        this.end = end;
        this.text = text;
    }
}
