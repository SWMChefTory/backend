package com.cheftory.api.recipe.ingredients.entity.converter;

import com.cheftory.api._common.GenericJsonConverter;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class IngredientJsonConverter extends GenericJsonConverter<List<Ingredient>> {

  protected IngredientJsonConverter(
      TypeReference<List<Ingredient>> typeReference) {
  }

}
