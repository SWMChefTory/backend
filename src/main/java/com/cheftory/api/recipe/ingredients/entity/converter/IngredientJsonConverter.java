package com.cheftory.api.recipe.ingredients.entity.converter;

import com.cheftory.api._common.GenericJsonConverter;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import java.util.List;

@Converter(autoApply = false)
public class IngredientJsonConverter extends GenericJsonConverter<List<Ingredient>> {

  public IngredientJsonConverter() {
    super(new TypeReference<List<Ingredient>> (){});
  }
}
