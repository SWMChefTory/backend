package com.cheftory.api.recipeinfo.history.entity;

import com.cheftory.api._common.Clock;
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
public class RecipeHistory {
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
    return RecipeHistory.builder()
        .id(UUID.randomUUID())
        .lastPlaySeconds(0)
        .viewedAt(clock.now())
        .createdAt(clock.now())
        .userId(userId)
        .recipeId(recipeId)
        .status(RecipeHistoryStatus.ACTIVE)
        .updatedAt(clock.now())
        .build();
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

  public void block() {
    this.status = RecipeHistoryStatus.BLOCKED;
    this.updatedAt = LocalDateTime.now();
  }

  public void delete() {
    this.status = RecipeHistoryStatus.DELETED;
    this.updatedAt = LocalDateTime.now();
  }
}
