package com.example.mockrecipecreator.dto;

import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@ToString
public class ClientIngredientsResponse {
    private List<Ingredient> ingredients;
}
