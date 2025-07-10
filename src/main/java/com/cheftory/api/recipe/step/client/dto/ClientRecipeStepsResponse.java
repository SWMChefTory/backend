package com.cheftory.api.recipe.step.client.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ClientRecipeStepsResponse {
    private CookingProcessSummary summary;

    @Getter
    public static class CookingProcessSummary {
        private String description;
        private List<ClientRecipeStepResponse> steps;
    }
}
