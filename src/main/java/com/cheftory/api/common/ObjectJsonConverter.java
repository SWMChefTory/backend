package com.cheftory.api.common.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

@Converter
public class ObjectJsonConverter implements AttributeConverter<Object, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
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
    public Object convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {
            });
        } catch (Exception e) {
            // 문구 바꾸기
            throw new IllegalArgumentException("Failed to deserialize JSON to list of strings", e);
        }
    }
}
