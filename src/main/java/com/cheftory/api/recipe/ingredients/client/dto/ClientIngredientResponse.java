package com.cheftory.api.recipe.ingredients.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class ClientIngredientResponse {
  private String name;
  private Integer amount;
  private String unit;
}
