package com.cheftory.api.recipe.viewstatus;

import com.cheftory.api._common.Clock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Builder(access=AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeViewStatus {
  @Id
  private UUID id;
  @Column(nullable = false)
  private LocalDateTime viewedAt;
  private Integer lastPlaySeconds;
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @Column(nullable = false)
  private UUID userId;
  @Column(nullable = false)
  private UUID recipeId;

  public static RecipeViewStatus of(Clock clock, UUID userId, UUID recipeId) {
    return RecipeViewStatus.builder()
        .id(UUID.randomUUID())
        .lastPlaySeconds(0)
        .viewedAt(clock.now())
        .createdAt(clock.now())
        .userId(userId)
        .recipeId(recipeId)
        .build();
  }

  public void updateViewedAt(Clock clock) {
    this.viewedAt = clock.now();
  }
}
