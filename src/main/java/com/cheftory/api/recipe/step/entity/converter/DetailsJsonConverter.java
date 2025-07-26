package com.cheftory.api.recipe.step.entity.converter;

import com.cheftory.api._common.GenericJsonConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import java.util.List;

@Converter(autoApply = false)
public class DetailsJsonConverter extends GenericJsonConverter<List<String>> {
  public DetailsJsonConverter() {
    super(new TypeReference<List<String>> (){});
  }
}
