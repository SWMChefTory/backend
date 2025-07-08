package com.cheftory.api.recipe.step.client.dto;
import lombok.Getter;
import java.util.List;

@Getter
public class ClientRecipeStepResponse {
    private String subtitle;
    private List<String> details;
    private Double start;
    private Double end;
}
