package com.cheftory.api.recipe.step.client.dto;

import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ClientRecipeStepRequest {
    private CaptionInfo captionInfo;
    private List<Ingredient> ingredients;

    public static ClientRecipeStepRequest from(CaptionInfo captionInfo, List<Ingredient> ingredients){
        return ClientRecipeStepRequest.builder()
                .captionInfo(captionInfo)
                .ingredients(ingredients).build();
    }
}
