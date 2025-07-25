package com.cheftory.api.recipe.caption.entity;

import com.cheftory.api.recipe.caption.entity.converter.SegmentsJsonConverter;
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
public class RecipeCaption {
    @Id
    @UuidGenerator
    private UUID id;

    @Convert(converter = SegmentsJsonConverter.class)
    @Column(columnDefinition = "json")
    private List<Segment> segments;

    @Enumerated(EnumType.STRING)
    private LangCodeType langCode;

    private UUID recipeId;

    public static RecipeCaption from(List<Segment> segments, LangCodeType langCodeType, UUID recipeId) {
        return RecipeCaption.builder()
                .segments(segments)
                .langCode(langCodeType)
                .recipeId(recipeId)
                .build();
    }
}
