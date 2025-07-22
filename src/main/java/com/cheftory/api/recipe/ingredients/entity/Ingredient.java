package com.cheftory.api.recipe.ingredients.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Ingredient {
    private String name;
    private Integer amount;
    private String unit;

    public static Ingredient of(String name, Integer amount, String unit) {
        return Ingredient.builder()
            .name(name)
            .amount(amount)
            .unit(unit)
            .build();
    }
}
