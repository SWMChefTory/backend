package com.cheftory.api.recipeviewstate.entity;

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
import org.hibernate.annotations.UuidGenerator;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeViewState {
  @Id
  @UuidGenerator
  private UUID id;
  private LocalDateTime viewedAt;
  private Integer lastPlaySeconds;
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @Column(nullable = false)
  private UUID userId;
  @Column(nullable = false)
  private UUID recipeId;

  public static RecipeViewState of(Clock clock, UUID userId, UUID recipeId) {
    return RecipeViewState.builder()
        .lastPlaySeconds(0)
        .createdAt(clock.now())
        .userId(userId)
        .recipeId(recipeId)
        .build();
  }
}
