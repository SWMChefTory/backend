package com.cheftory.api.recipe.step.dto;
import lombok.Getter;
import java.util.List;

@Getter
public class ClientStepResponse {
    private String subtitle;
    private List<String> details;
    private Double start;
    private Double end;
}
