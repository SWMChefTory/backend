package com.cheftory.api.recipe.step.entity.converter;

import com.cheftory.api._common.GenericJsonConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class DetailsJsonConverter extends GenericJsonConverter<List<String>> {
  public DetailsJsonConverter(
      TypeReference<List<String>> typeReference) {
    super(typeReference);
  }
}
