package com.cheftory.api.recipe.step.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ClientStepsResponse {
    List<ClientStepResponse> steps;
}
