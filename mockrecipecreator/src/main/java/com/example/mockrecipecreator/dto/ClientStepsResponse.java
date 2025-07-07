package com.example.mockrecipecreator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ClientStepsResponse {
    private List<ClientStepResponse> steps;
}

