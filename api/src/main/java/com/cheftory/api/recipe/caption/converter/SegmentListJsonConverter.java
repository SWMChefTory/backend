package com.cheftory.api.recipe.caption.converter;

import com.cheftory.api.recipe.caption.entity.Segment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class SegmentListJsonConverter implements AttributeConverter<List<Segment>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Segment> attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize segments to JSON", e);
        }
    }

    @Override
    public List<Segment> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<Segment>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize JSON to segments", e);
        }
    }
}