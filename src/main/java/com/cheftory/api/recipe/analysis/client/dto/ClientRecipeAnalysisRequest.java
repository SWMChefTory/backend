package com.cheftory.api.recipe.analysis.client.dto;

import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClientRecipeAnalysisRequest(
    @JsonProperty("video_id")
    String videoId,
    @JsonProperty("captions")
    List<Caption> captions
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

    public static ClientRecipeAnalysisRequest from(String videoId, RecipeCaption recipeCaption) {
        return new ClientRecipeAnalysisRequest(
            videoId,
            recipeCaption.getSegments().stream().map(
                Caption::from
            ).toList()
        );
    }
}
