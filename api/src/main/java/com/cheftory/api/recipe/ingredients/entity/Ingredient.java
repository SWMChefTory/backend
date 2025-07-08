package com.cheftory.api.recipe.ingredients.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Setter
public class Ingredient {
    private String name;
    private Integer amount;
    private String unit;
}
