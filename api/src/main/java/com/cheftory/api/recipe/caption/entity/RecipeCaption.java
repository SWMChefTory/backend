package com.cheftory.api.recipe.caption.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCaption {//caption
    @Id
    @UuidGenerator
    private UUID id;

    private String segments;
    private String langCode;

    private UUID recipeInfoId; //id

    public static RecipeCaption from(String segments, UUID recipeInfoId){
        return RecipeCaption.builder()
                .segments(segments)
                .recipeInfoId(recipeInfoId)
                .build();
    }
}
