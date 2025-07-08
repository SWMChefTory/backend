package com.cheftory.api.recipe.caption.entity;

import com.cheftory.api.recipe.caption.converter.SegmentListJsonConverter;
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
public class RecipeCaption {//caption
    @Id
    @UuidGenerator
    private UUID id;

    @Convert(converter = SegmentListJsonConverter.class)
    @Column(columnDefinition = "json")
    private List<Segment> segments;

    @Enumerated(EnumType.STRING)
    private LangCodeType langCode;

    private UUID recipeInfoId; //id

    public static RecipeCaption from(List<Segment> segments,LangCodeType langCodeType ,UUID recipeInfoId){
        return RecipeCaption.builder()
                .segments(segments)
                .langCode(langCodeType)
                .recipeInfoId(recipeInfoId)
                .build();
    }
}
