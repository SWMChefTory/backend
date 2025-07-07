package com.example.mockrecipecreator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Ingredient {
    private String name;
    private Integer amount;
    private String unit;

    public Ingredient() {}

    public Ingredient(String name, Integer amount, String unit) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }
}