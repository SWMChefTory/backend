package com.cheftory.api.recipe.content.tag.entity;

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeTag extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String tag;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private UUID recipeId;

    public static RecipeTag create(String tag, UUID recipeId, Clock clock) {
        return new RecipeTag(UUID.randomUUID(), tag, clock.now(), recipeId);
    }
}
