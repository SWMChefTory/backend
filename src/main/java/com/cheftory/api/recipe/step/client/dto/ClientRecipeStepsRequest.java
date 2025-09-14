package com.cheftory.api.recipe.step.client.dto;

import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record ClientRecipeStepsRequest(
    @JsonProperty("captions")
    List<Caption> recipeCaptions
) {
    private record Caption(
        @JsonProperty("start")
        Double start,
        @JsonProperty("end")
        Double end,
        @JsonProperty("text")
        String text
    ) {
        public static Caption from(RecipeCaption.Segment segment) {
            return new Caption(
                segment.getStart(),
                segment.getEnd(),
                segment.getText()
            );
        }
    }
    public static ClientRecipeStepsRequest from(RecipeCaption recipeCaption) {
        return new ClientRecipeStepsRequest(
            recipeCaption.getSegments()
                .stream()
                .map(Caption::from)
                .toList()
        );
    }
}
