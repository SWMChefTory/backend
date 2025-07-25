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
    @UuidGenerator
    @Id
    private UUID id;

    private Integer stepOrder;

    private String subtitle;

    @Column(columnDefinition = "json")
    @Convert(converter = DetailsJsonConverter.class)
    private List<String> details;
    private Double start;
    private Double end;

    private UUID recipeId;


    public static RecipeStep from(Integer stepOrder, String subtitle, List<String> details, Double start, Double end, UUID recipeId) {
        return RecipeStep.builder()
                .stepOrder(stepOrder)
                .subtitle(subtitle)
                .details(details)
                .start(start)
                .end(end)
                .recipeId(recipeId)
                .build();
    }
}
