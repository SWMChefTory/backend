package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;

public enum RecipeInfoRecommendType {
    POPULAR,
    TRENDING,
    CHEF;

    public static RecipeInfoRecommendType fromString(String type) throws RecipeException {
        try {
            return RecipeInfoRecommendType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new RecipeException(RecipeErrorCode.INVALID_RECOMMEND_TYPE);
        }
    }
}
