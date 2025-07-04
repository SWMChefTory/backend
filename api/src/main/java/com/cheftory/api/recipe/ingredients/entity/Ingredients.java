package com.cheftory.api.recipe.ingredients.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class Ingredients {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(columnDefinition = "json")
    private String content; //ingredients/id -> ingredient

    private UUID recipeInfoId;

    public static Ingredients from(String content, UUID recipeInfoId) {
        return Ingredients.builder()
                .content(content)
                .recipeInfoId(recipeInfoId)
                .build();
    }
}
