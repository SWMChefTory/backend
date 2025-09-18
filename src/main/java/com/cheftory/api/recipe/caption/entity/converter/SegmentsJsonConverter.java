package com.cheftory.api.recipe.caption.entity.converter;

import com.cheftory.api._common.GenericJsonConverter;
import com.cheftory.api.recipe.caption.entity.RecipeCaption.Segment;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import java.util.List;

@Converter(autoApply = false)
public class SegmentsJsonConverter extends GenericJsonConverter<List<Segment>> {

  public SegmentsJsonConverter() {
    super(new TypeReference<List<Segment>>() {});
  }
}
