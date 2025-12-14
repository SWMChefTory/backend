package com.cheftory.api.recipeinfo.progress.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeProgress extends MarketScope {
  @Id private UUID id;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RecipeProgressStep step;

  @Column(nullable = false)
  private RecipeProgressDetail detail;

  @Column(nullable = false)
  private UUID recipeId;

  public static RecipeProgress create(
      UUID recipeId, Clock clock, RecipeProgressStep step, RecipeProgressDetail detail) {
    return RecipeProgress.builder()
        .id(UUID.randomUUID())
        .createdAt(clock.now())
        .recipeId(recipeId)
        .step(step)
        .detail(detail)
        .build();
  }
}
