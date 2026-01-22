package com.cheftory.api.recipe.content.caption.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCaption extends MarketScope {
    @Id
    private UUID id;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Segment {
        private String text;
        private Double start;
        private Double end;
    }

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Segment> segments;

    @Enumerated(EnumType.STRING)
    private LangCodeType langCode;

    private UUID recipeId;

    private LocalDateTime createdAt;

    public static RecipeCaption from(List<Segment> segments, LangCodeType langCodeType, UUID recipeId, Clock clock) {

        return new RecipeCaption(UUID.randomUUID(), segments, langCodeType, recipeId, clock.now());
    }
}
