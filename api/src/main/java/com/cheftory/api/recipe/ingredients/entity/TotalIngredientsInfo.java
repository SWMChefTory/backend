package com.cheftory.api.recipe.ingredients.entity;

import jakarta.persistence.Embeddable;
import lombok.ToString;

import java.util.List;

@ToString
@Embeddable
public class TotalIngredientsInfo {
    List<IngredientInfo> ingredientInfos;
}
