package com.cheftory.api.recipe.ingredients.client.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Ingredient {
    private String name;
    private Integer amount;
    private String unit;
}
