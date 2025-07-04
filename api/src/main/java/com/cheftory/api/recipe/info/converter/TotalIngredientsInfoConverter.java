package com.cheftory.api.recipe.info.converter;

import com.cheftory.api.recipe.ingredients.entity.TotalIngredientsInfo;
import com.cheftory.api.recipe.info.converter.exception.TotalIngredientsInfoFormatException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TotalIngredientsInfoConverter implements AttributeConverter<TotalIngredientsInfo,String> {
    @Override
    public String convertToDatabaseColumn(TotalIngredientsInfo totalIngredientsInfo){
        return (totalIngredientsInfo==null)?null:totalIngredientsInfo.toString();
    }
    @Override
    public TotalIngredientsInfo convertToEntityAttribute(String totalIngredientsInfoJson){
        ObjectMapper mapper = new ObjectMapper();
        TotalIngredientsInfo totalIngredientsInfo;
        try {
            totalIngredientsInfo= mapper.readValue(totalIngredientsInfoJson, TotalIngredientsInfo.class);
        }catch (Exception e){
            throw new TotalIngredientsInfoFormatException(e.getMessage());
        }
        return totalIngredientsInfo;
    }
}
