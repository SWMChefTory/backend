package com.cheftory.api.recipe.ingredients.client.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ClientIngredientsResponse {
    private List<Ingredient> ingredients;
}
