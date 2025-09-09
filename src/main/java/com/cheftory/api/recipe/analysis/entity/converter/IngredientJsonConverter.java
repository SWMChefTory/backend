package com.cheftory.api.recipe.analysis.entity.converter;

import com.cheftory.api._common.GenericJsonConverter;
import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import java.util.List;

@Converter(autoApply = false)
public class IngredientJsonConverter extends GenericJsonConverter<List<RecipeAnalysis.Ingredient>> {

  public IngredientJsonConverter() {
    super(new TypeReference<List<RecipeAnalysis.Ingredient>> (){});
  }
}
