package com.cheftory.api.recipe.ingredients.helper;

public class RecipeIngredientsNotFoundException extends RuntimeException{
    public RecipeIngredientsNotFoundException(String message) {
        super(message);
    }
}
