package com.cheftory.api.recipe.analysis.entity.converter;

import com.cheftory.api._common.GenericJsonConverter;
import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis.Ingredient;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import java.util.List;


@Converter(autoApply = false)
public class TagJsonConverter extends GenericJsonConverter<List<String>> {

  public TagJsonConverter() {
    super(new TypeReference<List<String>>(){});
  }
}
