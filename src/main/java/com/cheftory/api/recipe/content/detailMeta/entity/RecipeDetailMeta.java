package com.cheftory.api.recipe.content.detailMeta.entity;

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
public class RecipeDetailMeta extends MarketScope {
  @Id private UUID id;

  @Column(nullable = false)
  private Integer cookTime;

  @Column(nullable = false)
  private Integer servings;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private UUID recipeId;

  public static RecipeDetailMeta create(
      Integer cookTime, Integer servings, String description, Clock clock, UUID recipeId) {

    return new RecipeDetailMeta(
        UUID.randomUUID(), cookTime, servings, description, clock.now(), recipeId);
  }
}
