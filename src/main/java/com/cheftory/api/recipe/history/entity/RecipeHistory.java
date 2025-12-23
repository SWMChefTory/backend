package com.cheftory.api.recipe.history.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
          name = "uq_recipe_history_user_recipe",
          columnNames = {"user_id", "recipe_id"})
    })
public class RecipeHistory extends MarketScope {
  @Id private UUID id;

  @Column(nullable = false)
  private LocalDateTime viewedAt;

  private Integer lastPlaySeconds;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private UUID recipeId;

  @Column(nullable = true)
  private UUID recipeCategoryId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private RecipeHistoryStatus status;

  public static RecipeHistory create(Clock clock, UUID userId, UUID recipeId) {
    LocalDateTime now = clock.now();

    return new RecipeHistory(
        UUID.randomUUID(), now, 0, now, now, userId, recipeId, null, RecipeHistoryStatus.ACTIVE);
  }

  public void updateRecipeCategoryId(UUID recipeCategoryId) {
    this.recipeCategoryId = recipeCategoryId;
  }

  public void emptyRecipeCategoryId() {
    this.recipeCategoryId = null;
  }

  public void updateViewedAt(Clock clock) {
    this.viewedAt = clock.now();
  }

  public void block(Clock clock) {
    this.status = RecipeHistoryStatus.BLOCKED;
    this.updatedAt = clock.now();
  }

  public void delete(Clock clock) {
    this.status = RecipeHistoryStatus.DELETED;
    this.updatedAt = clock.now();
  }

  public void active(Clock clock) {
    this.status = RecipeHistoryStatus.ACTIVE;
    this.updatedAt = clock.now();
  }
}
