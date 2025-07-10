package com.cheftory.api.recipe.step.entity;


import com.cheftory.api.common.converter.StringListJsonConverter;
import com.cheftory.api.recipe.step.client.dto.ClientRecipeStepResponse;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
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
    @Convert(converter = StringListJsonConverter.class)
    private List<String> details;
    private Double start;
    private Double end;

    private UUID recipeId;


    public static RecipeStep from(Integer stepOrder, ClientRecipeStepResponse stepResponses, UUID recipeId) {
        return RecipeStep.builder()
                .stepOrder(stepOrder)
                .subtitle(stepResponses.getSubtitle())
                .details(stepResponses.getDescriptions())
                .start(stepResponses.getStart())
                .end(stepResponses.getEnd())
                .recipeId(recipeId)
                .build();
    }

    public RecipeStepInfo toRecipeStepInfo() {
        return RecipeStepInfo.from(
                this);
    }
}
