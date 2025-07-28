package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.caption.entity.LangCodeType;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.entity.Segment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CaptionInfo {
    @JsonProperty("lang_code")
    LangCodeType langCodeType;
    @JsonProperty("captions")
    List<Segment> captions;

    public static CaptionInfo from(RecipeCaption caption) {
        return CaptionInfo
                .builder()
                .langCodeType(caption.getLangCode())
                .captions(caption.getSegments())
                .build();
    }
}
