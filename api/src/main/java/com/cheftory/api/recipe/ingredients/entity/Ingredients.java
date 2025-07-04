package com.cheftory.api.recipe.ingredients.entity;

import com.cheftory.api.recipe.info.entity.RecipeInfo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class RequiredIngredients{
    @Id
    @UuidGenerator
    private UUID id;
    @Column(columnDefinition = "json")
    private String content;

    private UUID recipeInfoId;

    public static RequiredIngredients from(String content, UUID recipeInfoId) {
        return RequiredIngredients.builder()
                .content(content)
                .recipeInfoId(recipeInfoId)
                .build();
    }
}
