package com.cheftory.api.recipe.analysis.entity;

import com.cheftory.api.recipe.analysis.entity.converter.IngredientJsonConverter;
import com.cheftory.api.recipe.analysis.entity.converter.TagJsonConverter;
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
public class RecipeAnalysis {
    @Id
    private UUID id;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Ingredient {
        private String name;
        private Integer amount;
        private String unit;
    }

    @Column(columnDefinition = "json")
    @Convert(converter = IngredientJsonConverter.class)
    private List<Ingredient> ingredients;

    @Column(columnDefinition = "json")
    @Convert(converter = TagJsonConverter.class)
    private List<String> tags;

    private Integer servings;
    private Integer cookTime;
    private String description;
    private UUID recipeId;

    public static RecipeAnalysis from(List<Ingredient> ingredients, UUID recipeId, List<String> tags, Integer servings, Integer cookTime, String description) {
        return RecipeAnalysis.builder()
                .id(UUID.randomUUID())
                .ingredients(ingredients)
                .recipeId(recipeId)
                .tags(tags)
                .servings(servings)
                .cookTime(cookTime)
                .description(description)
                .build();
    }

}
