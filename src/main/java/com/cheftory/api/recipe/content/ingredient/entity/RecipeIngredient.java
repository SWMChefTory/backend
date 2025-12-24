package com.cheftory.api.recipe.content.ingredient.entity;

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
public class RecipeIngredient extends MarketScope {
  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String unit;

  @Column(nullable = false)
  private Integer amount;

  @Column(nullable = false)
  private UUID recipeId;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public static RecipeIngredient create(
      String name, String unit, Integer amount, UUID recipeId, Clock clock) {

    return new RecipeIngredient(UUID.randomUUID(), name, unit, amount, recipeId, clock.now());
  }
}
