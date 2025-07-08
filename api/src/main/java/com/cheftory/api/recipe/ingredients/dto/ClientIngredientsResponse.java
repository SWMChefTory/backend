package com.cheftory.api.recipe.ingredients.dto;

import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Setter
public class ClientIngredientsResponse {
    private List<Ingredient> ingredients;
}
