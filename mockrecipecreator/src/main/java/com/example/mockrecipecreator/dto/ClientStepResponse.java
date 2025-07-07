package com.example.mockrecipecreator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ClientStepResponse {
    private String subtitle;
    private List<String> details;
    private Double start;
    private Double end;

    public ClientStepResponse() {}

    public ClientStepResponse(String subtitle, List<String> details, Double start, Double end) {
        this.subtitle = subtitle;
        this.details = details;
        this.start = start;
        this.end = end;
    }
}
