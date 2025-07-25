package com.cheftory.api.recipe.caption.entity.converter;

import com.cheftory.api._common.GenericJsonConverter;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class SegmentsJsonConverter extends GenericJsonConverter<List<Segment>> {

  protected SegmentsJsonConverter(
      TypeReference<List<Segment>> typeReference) {
    super(typeReference);
  }
}
