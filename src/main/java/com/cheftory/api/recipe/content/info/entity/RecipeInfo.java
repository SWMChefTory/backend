package com.cheftory.api.recipe.content.info.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "recipe")
public class RecipeInfo extends MarketScope {
  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProcessStep processStep;

  @Column(nullable = false)
  private Integer viewCount;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  private RecipeStatus recipeStatus;

  @Column(nullable = false)
  private long creditCost;

  public static RecipeInfo create(Clock clock) {
    LocalDateTime now = clock.now();
    return new RecipeInfo(
        UUID.randomUUID(), ProcessStep.READY, 0, now, now, RecipeStatus.IN_PROGRESS, 1L);
  }

  public void success(Clock clock) {
    this.updatedAt = clock.now();
    this.recipeStatus = RecipeStatus.SUCCESS;
  }

  public void failed(Clock clock) {
    this.updatedAt = clock.now();
    this.recipeStatus = RecipeStatus.FAILED;
  }

  public void block(Clock clock) {
    this.updatedAt = clock.now();
    this.recipeStatus = RecipeStatus.BLOCKED;
  }

  public boolean isSuccess() {
    return this.recipeStatus == RecipeStatus.SUCCESS;
  }

  public boolean isFailed() {
    return this.recipeStatus == RecipeStatus.FAILED;
  }

  public boolean isBlocked() {
    return this.recipeStatus == RecipeStatus.BLOCKED;
  }
}
