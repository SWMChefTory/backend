package com.cheftory.api.recipe.step.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeStep {
    @UuidGenerator
    @Id
    private UUID id;
    private Integer order;
    private String subtitle;
    private String details;
    private Double start;
    private Double end;

    private UUID recipeInfoId;

    public static RecipeStep from(
            String subtitle
            ,Integer order
            , String details
            , Double start
            , Double end
            , UUID recipeInfoId) {
        return RecipeStep.builder()
                .subtitle(subtitle)
                .order(order)
                .details(details)
                .start(start)
                .end(end)
                .recipeInfoId(recipeInfoId)
                .build();
    }
}
