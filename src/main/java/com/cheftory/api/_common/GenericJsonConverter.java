package com.cheftory.api._common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

/**
 * 제네릭 JSON 변환기.
 *
 * <p>JPA 엔티티의 JSON 타입 필드를 데이터베이스 문자열로 변환합니다.</p>
 *
 * @param <T> 변환할 제네릭 타입
 */
@Converter
public class GenericJsonConverter<T> implements AttributeConverter<T, String> {

    /** JSON 변환을 위한 ObjectMapper */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /** 타입 레퍼런스 */
    private final TypeReference<T> typeReference;

    /**
     * GenericJsonConverter 인스턴스를 생성합니다.
     *
     * @param typeReference 타입 레퍼런스
     */
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
            throw new IllegalArgumentException("Failed to deserialize JSON to list of strings", e);
        }
    }
}
