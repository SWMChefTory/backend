package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.Getter;

@Getter
public enum RecipeCuisineType {
    KOREAN,
    SNACK,
    CHINESE,
    JAPANESE,
    WESTERN,
    DESSERT,
    HEALTHY,
    BABY,
    SIMPLE;

    public String messageKey() {
        return "recipe.cuisine." + name().toLowerCase();
    }

    public static RecipeCuisineType fromString(String type) {
        try {
            return valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new RecipeException(RecipeErrorCode.INVALID_CUISINE_TYPE);
        }
    }
}
