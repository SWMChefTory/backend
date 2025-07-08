package com.cheftory.api.recipe.ingredients.dto;

import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class ClientIngredientsResponse {
    private List<Ingredient> ingredients;
}
