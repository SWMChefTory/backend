package com.cheftory.api.recipe.creation.identify.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.*;
import java.net.URI;
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
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_recipe_identify_market_video",
                    columnNames = {"market", "url"})
        })
public class RecipeIdentify extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private URI url;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static RecipeIdentify create(URI url, UUID recipeId, Clock clock) {
        return new RecipeIdentify(UUID.randomUUID(), url, recipeId, clock.now());
    }
}
