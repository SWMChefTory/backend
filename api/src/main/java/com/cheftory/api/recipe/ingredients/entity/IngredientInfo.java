package com.cheftory.api.recipe.ingredients.entity;

import lombok.*;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class IngredientInfo {
    String name;
    Integer amount;
    String unit;

    public static IngredientInfo from(String name, Integer amount, String unit){
        return IngredientInfo.builder()
                .name(name)
                .amount(amount)
                .build();
    }
}
