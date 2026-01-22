package com.cheftory.api.recipe.content.briefing.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeBriefing extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static RecipeBriefing create(UUID recipeId, String content, Clock clock) {
        return new RecipeBriefing(UUID.randomUUID(), recipeId, content, clock.now());
    }
}
