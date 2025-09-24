package com.cheftory.api._common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

@Converter
public class GenericJsonConverter<T> implements AttributeConverter<T, String> {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final TypeReference<T> typeReference;

  protected GenericJsonConverter(TypeReference<T> typeReference) {
    this.typeReference = typeReference;
  }

  @Override
  public String convertToDatabaseColumn(T attribute) {
    if (attribute == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (Exception e) {
      // 문구 바꾸기
      throw new IllegalArgumentException("Failed to serialize list of strings", e);
    }
  }

  @Override
  public T convertToEntityAttribute(String dbData) {
    if (StringUtils.isBlank(dbData)) {
      return null;
    }
    try {
      return objectMapper.readValue(dbData, typeReference);
    } catch (Exception e) {
      // 문구 바꾸기
      throw new IllegalArgumentException("Failed to deserialize JSON to list of strings", e);
    }
  }
}
