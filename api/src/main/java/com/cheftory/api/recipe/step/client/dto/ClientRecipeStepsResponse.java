package com.cheftory.api.recipe.step.client.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ClientRecipeStepsResponse {
    List<ClientRecipeStepResponse> steps;
}
