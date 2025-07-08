package com.cheftory.api.recipe.ingredients.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class Ingredient {
    private String name;
    private Integer amount;
    private String unit;
}
