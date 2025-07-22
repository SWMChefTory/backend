package com.cheftory.api.recipe.ingredients.client.dto;

import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class ClientIngredientsResponse {
    private List<ClientIngredientResponse> ingredients;
}
