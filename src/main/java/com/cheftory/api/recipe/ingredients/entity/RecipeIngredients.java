package com.cheftory.api.recipe.ingredients.entity;

import com.cheftory.api._common.GenericJsonConverter;
import com.cheftory.api.recipe.ingredients.entity.converter.IngredientJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class RecipeIngredients {
    @Id
    private UUID id;

    //converter로 달아줘야 함.
    @Column(columnDefinition = "json")
    @Convert(converter = IngredientJsonConverter.class)
    private List<Ingredient> ingredients;

    private UUID recipeId;

    public static RecipeIngredients from(List<Ingredient> ingredients, UUID recipeId) {
        return RecipeIngredients.builder()
                .id(UUID.randomUUID())
                .ingredients(ingredients)
                .recipeId(recipeId)
                .build();
    }

}
