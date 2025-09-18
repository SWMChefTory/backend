package com.cheftory.api.recipe.step.entity;

import com.cheftory.api.recipe.step.entity.converter.DetailsJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeStep {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail {
        private String text;
        private Double start;
    }

    @UuidGenerator
    @Id
    private UUID id;

    private Integer stepOrder;

    private String subtitle;

    @Column(columnDefinition = "json")
    @Convert(converter = DetailsJsonConverter.class)
    private List<Detail> details;

    private Double start;

    private UUID recipeId;

    public static RecipeStep from(
        Integer stepOrder,
        String subtitle,
        List<Detail> details,
        Double start,
        UUID recipeId
    ) {
        return RecipeStep.builder()
            .stepOrder(stepOrder)
            .subtitle(subtitle)
            .details(details)
            .start(start)
            .recipeId(recipeId)
            .build();
    }
}